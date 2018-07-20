package com.cehome.easysync.plugins;

import com.alibaba.fastjson.JSON;
import com.cehome.easykafka.Consumer;
import com.cehome.easykafka.consumer.ConsumerRecord;
import com.cehome.easykafka.consumer.SimpleKafkaConsumer;
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
import org.apache.commons.lang.StringUtils;
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
    private static AtomicLong clientId=new AtomicLong(0);

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
        if(consumer!=null)Common.closeObject(consumer);
        consumer = new SimpleKafkaConsumer(kafka.getVersion(),props);
        consumer.createKafkaConsumer();
        consumer.subscribe(Const.TOPIC_PREFIX + context.getId());
    }

    @Override
    public void run() {
        props.put("client.id",String.valueOf(context.getId() + atomicLong.incrementAndGet()));
        props.put("bootstrap.servers", kafka.getServers());
        props.put("group.id", "es_" + indexName);

        props.put("enable.auto.commit", "false");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("fetch.message.max.bytes", "10MB");
        /*solution for Caused by: org.apache.kafka.clients.consumer.CommitFailedException: Commit cannot be completed since the group has already rebalanced and assigned the partitions to another member. This means that the time between subsequent calls to poll() was longer than the configured max.poll.interval.ms, which typically implies that the poll loop is spending too much time message processing. You can address this either by increasing the session timeout or by reducing the maximum size of batches returned in poll() with max.poll.records.*/
        props.put("session.timeout.ms", "30000");

        //kafka在0.9版本无max.poll.records参数，默认拉取记录是500，直到0.10版本才引入该参数
        props.put("max.poll.records", "10");

        //消息发送的最长等待时间.需大于session.timeout.ms这个时间
        props.put("request.timeout.ms", "40000");



        props.put("client.id",""+clientId.incrementAndGet());
        if(StringUtils.isNotBlank(kafka.getConsumerConfigs())){
            String[] lines=kafka.getConsumerConfigs().split("[\\r\\n]+");
            for (String line:lines){
                line=line.trim();
                if(line.length()==0) continue;
                String[] e= line.split("\\s+=\\s+");
                props.put(e[0],e[1]);
            }
        }
       /* props.put("group.id", "es_" + indexName);
        props.put("enable.auto.commit", kafka.getEnableAutoCommit());
        props.put("key.deserializer", kafka.getKeyDeserializer());
        props.put("value.deserializer", kafka.getValueDeserializer());
        props.put("fetch.message.max.bytes", kafka.getFetchMessageMaxBytes());*/
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
