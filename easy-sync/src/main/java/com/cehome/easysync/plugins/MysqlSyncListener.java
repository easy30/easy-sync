package com.cehome.easysync.plugins;

import com.alibaba.fastjson.JSON;
import com.cehome.easykafka.Producer;
import com.cehome.easysync.domain.Position;
import com.cehome.easysync.objects.Column;
import com.cehome.easysync.objects.Row;
import com.cehome.easysync.objects.TableTask;
import com.cehome.easysync.service.DatabaseService;
import com.cehome.easysync.service.PositionSaveService;
import com.cehome.easysync.service.TableTasksMapService;
import com.cehome.easysync.utils.Const;
import com.cehome.easysync.utils.MysqlTypeUtils;
import com.cehome.task.client.TimeTaskContext;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import jsharp.util.TimeCal;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Component
@Scope("prototype")
public class MysqlSyncListener implements BinaryLogClient.EventListener {
    private static final Logger logger = LoggerFactory.getLogger(MysqlSyncListener.class);
    private Position position = null;
    private String database;
    private String table;
    private long tableId;
    private List<TableTask> tableTasks;
    BinaryLogClient client;
    DatabaseService databaseService;
    private long timezoneInMS;
    Producer producer;
    TimeTaskContext context;
    TimeCal timeCal1 = new TimeCal(5);
    TimeCal timeCal2 = new TimeCal(30);
    private volatile boolean running = true;


    @Autowired
    TableTasksMapService tableTasksMapService;


    private PositionSaveService positionSaveService;

    public MysqlSyncListener() {

    }

    public void init(TimeTaskContext context, Producer producer, BinaryLogClient client, DatabaseService databaseService, PositionSaveService positionSaveService) {
        this.context = context;
        this.producer = producer;
        this.client = client;
        this.databaseService = databaseService;
        this.positionSaveService = positionSaveService;
        this.timezoneInMS = databaseService.getTimezoneHours() * 3600 * 1000;

       /* position=positionService.getPosition(databaseService.getTimeTaskId(),databaseService.getServerId());
        if(position==null) {
            position = EntityUtils.create(Position.class);
            position.setTimeTaskId(databaseService.getTimeTaskId());
            position.setServerId(databaseService.getServerId());
        }*/


    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void onEvent(Event event) {
        try {
            doOnEvent(event);
        } catch (Throwable e) {
            //logger.error("event error", e);
            setRunning(false);
            // in order to jump out BinlogClient loop and end
            throw new Error(e);
               /* new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //disconnect in order to  restart with last postion, then will not lose any event.
                        client.disconnect();
                        logger.info("disconnect ok");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }).start();
*/


        }
    }


    public void doOnEvent(Event event) throws Exception {
        logger.debug("" + event);


        TimeCal timeCal = timeCal2;
        EventType eventType = event.getHeader().getEventType();
        if (eventType == EventType.GTID) {
            String gtid = ((GtidEventData) event.getData()).getGtid();
        }
        if (eventType == EventType.TABLE_MAP) {
            TableMapEventData data = (TableMapEventData) event.getData();
            database = data.getDatabase();
            table = data.getTable();
            tableId = data.getTableId();
            Map<String, List<TableTask>> tableTasksMap = tableTasksMapService.get(databaseService);
            tableTasks = tableTasksMap.get(database + "." + table);

        } else if (CollectionUtils.isNotEmpty(tableTasks)) {
            ArrayList<Row> list = new ArrayList<>();
            if (eventType == EventType.WRITE_ROWS || eventType == EventType.EXT_WRITE_ROWS) {
                WriteRowsEventData data = (WriteRowsEventData) event.getData();
                for (Serializable[] rowData : data.getRows()) {
                    list.add(buildRow("insert", rowData, data.getIncludedColumns()));
                }

            } else if (eventType == EventType.UPDATE_ROWS || eventType == EventType.EXT_UPDATE_ROWS) {

                UpdateRowsEventData data = (UpdateRowsEventData) event.getData();

                for (Map.Entry<Serializable[], Serializable[]> e : data.getRows()) {
                    Serializable[] rowData = e.getValue();
                    list.add(buildRow("update", rowData, data.getIncludedColumns()));
                }


            } else if (eventType == EventType.DELETE_ROWS || eventType == EventType.EXT_DELETE_ROWS) {
                DeleteRowsEventData data = (DeleteRowsEventData) event.getData();
                for (Serializable[] rowData : data.getRows()) {
                    list.add(buildRow("delete", rowData, data.getIncludedColumns()));
                }
            }


            //todo: 事务性
            for (TableTask tableTask : tableTasks) {

                for (Row row : list) {

                    String id = row.getData().getStr(tableTask.getKeys());
                    try {
                        send(Const.TOPIC_PREFIX + tableTask.getTimeTaskId(), id, JSON.toJSONString(row));
                        logger.info("[binlog] success task={}, {}", tableTask.getTimeTaskId(), JSON.toJSONString(row));
                    } catch (Exception e) {
                        logger.error("[binlog] fail task={}, {}", tableTask.getTimeTaskId(), JSON.toJSONString(row));
                        throw e;
                    }
                    //logger

                }

            }

            timeCal = timeCal1;


        }


        positionSaveService.set(client.getBinlogFilename(), client.getBinlogPosition(), client.getGtidSet());
        if (timeCal.isTimeUp() > 0) {
            positionSaveService.save();
        }


    }


    private void send(String topic, String key, String value) throws Exception {
        final int count = 20;
        for (int i = 0; i < count; i++) {
            if (!context.isRunning()) {
                throw new RuntimeException("Task is stopped, send end.");
            }
            try {
                producer.send(topic, key, value);
                return;
            } catch (Exception e) {
                if (i == count - 1) {
                    logger.error("try {} times and exit", count);
                    throw e;
                } else {
                    if (i == 0) {
                        logger.error("send error", e);
                    } else {
                        logger.error("send error: {}", e.getMessage());
                    }
                    context.sleep(5000);
                }

            }
        }

    }


    private Row buildRow(String type, Serializable[] rowData, BitSet includedColumns) throws Exception {
        Row row = new Row();
        row.setType(type);
        row.setDatabase(database);
        row.setTable(table);
        int dataIdx = 0;
        List<Column> columns = databaseService.getColumns(database, table);
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            if (includedColumns.get(i)) {
                Object value = null;
                if (rowData[dataIdx] != null) {
                    value = convertColumn(column, rowData[dataIdx]);
                }
                row.getData().put(column.getName(), value);
                dataIdx++;
            }

        }

        return row;

    }

    /**
     * 数字型、日期型转long 不变
     * char/varchar/text 型转byte[]，需要根据字段编码还原
     * blob转byte[]
     *
     * @param object
     * @return
     */
    private Object convertColumn(Column column, Object object) throws UnsupportedEncodingException {
//https://www.cnblogs.com/waterystone/p/6226356.html

        if (object instanceof byte[]) {
            String dataType = column.getDataType();
            if (MysqlTypeUtils.isString(dataType)) {
                byte[] bs = (byte[]) object;
                return column.getCharset() == null ? new String(bs) : new String(bs, column.getCharset());
            }
        } else if (object instanceof Long) {
            String dataType = column.getDataType();
            if (MysqlTypeUtils.isDateNormal(dataType)) {
                return ((Long) object) - timezoneInMS;
            }
        }

        return object;

    }


}
