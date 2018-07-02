package com.cehome.easysync.plugins;

import com.alibaba.fastjson.JSON;
import com.cehome.easykafka.Consumer;
import com.cehome.easykafka.consumer.ConsumerRecord;
import com.cehome.easykafka.consumer.KafkaConsumer;
import com.cehome.easysync.jest.Jest;
import com.cehome.easysync.objects.Column;
import com.cehome.easysync.objects.Row;
import com.cehome.easysync.objects.config.EsConfig;
import com.cehome.easysync.objects.config.EsField;
import com.cehome.easysync.objects.config.Kafka;
import com.cehome.easysync.service.DatabaseService;
import com.cehome.easysync.utils.Const;
import com.cehome.easysync.utils.Global;
import com.cehome.task.client.TimeTaskContext;
import io.searchbox.core.Bulk;
import io.searchbox.core.BulkResult;
import io.searchbox.core.Delete;
import io.searchbox.core.Index;
import jsharp.util.Common;
import jsharp.util.DataMap;
import org.apache.commons.beanutils.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class EsSyncIncrement extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(EsSyncIncrement.class);

    private TimeTaskContext context;
    private Kafka kafka;
    private volatile String indexName;

    Jest jest;
    private EsConfig esConfig;
    private EsField[] esFields;
    Map<String,Column> columnMap;
    private Consumer consumer = null;
    DatabaseService databaseService;
    Properties props = new Properties();
    private volatile boolean running = true;
    private AtomicLong atomicLong = new AtomicLong();

    public EsSyncIncrement(TimeTaskContext context, DatabaseService databaseService, Kafka kafka, String indexName, Jest jest, String taskConfig) throws Exception {
        this.context = context;
        this.kafka = kafka;
        this.indexName = indexName;
        this.databaseService=databaseService;

        this.jest = jest;
        esConfig = Global.toObject(taskConfig, EsConfig.class);
        esFields= esConfig.getEsMapping().getEsFields();

    }

   /* public String getIndexName() {
        return indexName;
    }*/

    private void createKafkaConsumer() throws Exception{
        Common.closeObject(consumer);
        consumer = new KafkaConsumer(kafka.getVersion(),props);
        consumer.createKafkaConsumer();
        consumer.subscribe(Const.TOPIC_PREFIX + context.getId());
    }

    @Override
    public void run() {
        props.put("client.id",String.valueOf(context.getId() + atomicLong.incrementAndGet()));
        props.put("bootstrap.servers", kafka.getServers());
        props.put("group.id", "es_" + indexName);
        props.put("enable.auto.commit", kafka.getEnableAutoCommit());
        props.put("key.deserializer", kafka.getKeyDeserializer());
        props.put("value.deserializer", kafka.getValueDeserializer());
        props.put("fetch.message.max.bytes", kafka.getFetchMessageMaxBytes());
        final int minBatchSize = 10;
        MDC.put("shard", "task/"+context.getId());
        while (canRun()) {
            try {

                createKafkaConsumer();
                break;
            } catch (Exception e) {
                logger.error("", e);
                context.sleep(3000);
            }
        }

        //List<ConsumerRecord<String, String>> buffer = new ArrayList<>();

        while (canRun()) {

            try {
                List<ConsumerRecord> records = (List<ConsumerRecord>) consumer.poll(500);
                if(!records.isEmpty()) {
                    doSync(records);
                }
                consumer.commitSync();
            } catch (Exception e) {
                logger.error("", e);
                context.sleep(3000);


            }

        }
        Common.closeObject(consumer);

        MDC.remove("shard");
    }


    private boolean canRun() {
        return context.isRunning() && running;
    }


    public void terminate(long waitMS) {
        running = false;
        if (waitMS > 0) {
            try {
                this.join(waitMS);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
        }
    }


    private Map convertKafka2Es(Map<String, Object> dataMap) throws UnsupportedEncodingException {

        DataMap result=new DataMap();
        for(EsField esField :esFields){
            String name=esField.getSource();
            String targetType=esField.getType();
            Column column=columnMap.get(name);
            Object value=dataMap.get(name);
            if(value!=null) {
                if ("boolean".equalsIgnoreCase(targetType)) {
                    value = ConvertUtils.convert(value, Boolean.class);
                } else if ("date".equalsIgnoreCase(targetType)) {
                    if (value instanceof Date) {
                        value = ((Date) value).getTime();
                    }
                } else if ("binary".equalsIgnoreCase(targetType)) {
                    if (value instanceof byte[]) {
                        value = org.apache.commons.codec.binary.Base64.encodeBase64String((byte[]) value);
                    } else {
                        value = org.apache.commons.codec.binary.Base64.encodeBase64String(value.toString().getBytes("UTF-8"));
                    }
                }
            }

            result.put(esField.getTarget(),value);


        }


        return result;


    }

    private void doSync(List<ConsumerRecord> records) throws Exception {

        Bulk.Builder bulk = new Bulk.Builder().defaultIndex(indexName).defaultType(Const.ES_TYPE);
        for (ConsumerRecord record : records) {
            logger.info("[incr] {}={}",indexName,record.value());
            Row row = JSON.parseObject(record.value(), Row.class);
            if(columnMap==null){
                columnMap=databaseService.getColumnMap(row.getDatabase(),row.getTable());
            }
            String id = record.key();
            if (row.getType().equalsIgnoreCase("insert") || (row.getType().equalsIgnoreCase("update"))) {
                LinkedHashMap<String, Object> data = row.getData();
                Map map = (convertKafka2Es(data));
                Index index = new Index.Builder(map).id(id).build();
                bulk.addAction(index);

            } else if (row.getType().equalsIgnoreCase("delete")) {
                Delete delete = new Delete.Builder(id).build();
                bulk.addAction(delete);
            } else {
                //
            }
        }


            BulkResult br = jest.getJestClient().execute(bulk.build());
            if (!br.isSucceeded()) {
                logger.error("error={}, failItems={}", br.getErrorMessage(), JSON.toJSONString(br.getFailedItems()));
                //   br.getFailedItems().get(0).
                throw new RuntimeException("bulk error");
            }


            //   buffer.add(record);

    }
}
