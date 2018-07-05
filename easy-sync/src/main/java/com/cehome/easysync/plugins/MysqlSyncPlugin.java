package com.cehome.easysync.plugins;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cehome.easykafka.Producer;
import com.cehome.easykafka.producer.KafkaProducer;
import com.cehome.easysync.Application;
import com.cehome.easysync.domain.Position;
import com.cehome.easysync.objects.MysqlUrl;
import com.cehome.easysync.objects.config.Kafka;
import com.cehome.easysync.objects.config.Mysql;
import com.cehome.easysync.objects.config.MysqlConfig;
import com.cehome.easysync.service.DatabaseService;
import com.cehome.easysync.service.PositionSaveService;
import com.cehome.easysync.service.PositionService;
import com.cehome.easysync.service.TableTasksMapService;
import com.cehome.easysync.utils.Global;
import com.cehome.task.client.TimeTaskContext;
import com.cehome.task.client.TimeTaskPlugin;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import jsharp.util.EntityUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * --读取基本配置
 * ----初始化Database服务，获取serverid
 * -- 获取 last position. 通过serverid 和 timetaskId去查找位置，如果找不到
 *
 */
@Component
public class MysqlSyncPlugin extends TimeTaskPlugin {
    private static final Logger logger = LoggerFactory.getLogger(MysqlSyncPlugin.class);
    @Autowired
    private PositionService positionService;
    @Autowired
    TableTasksMapService tableTasksMapService;
    @Override
    public void run(TimeTaskContext context, JSONObject args) throws Exception {

        final String taskConfig=context.getTaskConfig();
        long timeTaskId=context.getId();
        MysqlConfig mysqlConfig= Global.toObject(taskConfig, MysqlConfig.class);
        Mysql mysql=mysqlConfig.getMysql();
        MysqlUrl mysqlUrl=new MysqlUrl(mysql.getUrl());
        logger.info("create database meta service");
        final DatabaseService databaseService=DatabaseService.newInstance(mysql);
        databaseService.setTimeTaskId(timeTaskId);
        long serverId=databaseService.getServerId();

        tableTasksMapService.clean(timeTaskId);

        logger.info("create kafka producer");
        Kafka kafka=mysqlConfig.getKafka();
        Properties props = new Properties();
        props.put("bootstrap.servers", kafka.getServers());
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("message.max.bytes", "10MB");
        props.put("replica.fetch.max.bytes", "10MB");
        if(StringUtils.isNotBlank(kafka.getProducerConfigs())){
            String[] lines=kafka.getProducerConfigs().split("[\\r\\n]+");
            for (String line:lines){
                line=line.trim();
                if(line.length()==0) continue;
                String[] e= line.split("\\s*=\\s*");
                props.put(e[0],e[1]);
            }
        }
        /*props.put("acks", kafka.getAcks());
        props.put("retries", kafka.getRetries());
        props.put("batch.size", kafka.getBatchSize());
        props.put("linger.ms", kafka.getLingerMs());
        props.put("buffer.memory", kafka.getBufferMemory());
        props.put("key.serializer", kafka.getKeySerializer());
        props.put("value.serializer", kafka.getValueSerializer());
        props.put("message.max.bytes", kafka.getMessageMaxBytes());
        props.put("replica.fetch.max.bytes", kafka.getReplicaFetchMaxBytes());*/

        Producer producer = new KafkaProducer(kafka.getVersion(),props);
        context.put("producer",producer);

        BinaryLogClient client =null;
        PositionSaveService positionSaveService=null;
        while(context.isRunning()) {
            try {
                logger.info("create BinaryLogClient");

                //client = new BinaryLogClient("192.168.0.13", 3306, "root", "asdf1234!");
                //client.setConnectTimeout(30 * 1000);
                client = new BinaryLogClient(mysqlUrl.getHost(), Integer.parseInt(mysqlUrl.getPort()), mysql.getUser() , mysql.getPassword());
                client.setConnectTimeout(mysqlUrl.getConnectTimeout());
                client.setKeepAliveInterval(5 * 1000);
                //client.setKeepAlive();
                EventDeserializer eventDeserializer = new EventDeserializer();
                eventDeserializer.setCompatibilityMode(
                        EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG,
                        EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY
                        // EventDeserializer.CompatibilityMode.In INVALID_DATE_AND_TIME_AS_MIN_VALUE
                );
                client.setEventDeserializer(eventDeserializer);
               logger.info("get last position");
                Position position=positionService.getPosition(timeTaskId,serverId);
                if (position == null) {
                    logger.info("last position is null. maybe first run");
                    position = EntityUtils.create(Position.class);
                    position.setTimeTaskId(databaseService.getTimeTaskId());
                    position.setServerId(databaseService.getServerId());
                } else {
                    logger.info("last position :" + JSON.toJSONString(position));
                    client.setBinlogFilename(position.getFilename());
                    client.setBinlogPosition(position.getPosition());
                    client.setGtidSet(position.getGtidSet());
                }
                 positionSaveService=new PositionSaveService(positionService,position);


                MysqlSyncListener mysqlSyncListener= Application.getBean(MysqlSyncListener.class);
                mysqlSyncListener.init(producer, client, databaseService,positionSaveService);
                client.registerEventListener(mysqlSyncListener);
                context.put("client", client);
                client.connect();
                while (mysqlSyncListener.isRunning()){
                    context.sleep(5000);
                }

            } catch (Throwable e) {
                logger.error("",e);
            }
            try {
                client.disconnect();
            } catch (Exception ex) {

            }
            if(positionSaveService!=null){
                positionSaveService.save();
            }
            context.sleep(5000);
        }

        System.out.println("task end");



        logger.info("task id="+context.getId());
        logger.info("task name="+context.getName());
        logger.info("task run on ip="+ Inet4Address.getLocalHost().getHostAddress());
        logger.info("task run count="+context.getRunTimes());
    }







    @Override
    public void stop(TimeTaskContext context) throws Exception {
        try {
            BinaryLogClient client = (BinaryLogClient) context.get("client");
            client.disconnect();
        }catch (Exception e){
            logger.error("client disconnect error",e);
        }
        Producer producer=(Producer)context.get("producer");
        producer.close();

        logger.info("task "+context.getName()+" is stopped ");
    }
}
