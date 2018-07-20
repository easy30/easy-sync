package com.cehome.easykafka;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.cehome.easykafka.producer.SimpleKafkaProducer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Random;

/**
 * Created by houyanlin on 2018/06/26
 **/
public class KafkaProducerTest {
    Logger logger = LoggerFactory.getLogger(KafkaProducerTest.class);
    static {
        ( (LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("ROOT").setLevel(Level.INFO);
    }

    @Test
    public void sendTest() throws Exception{
        String version="0.11.0.1";
        //version="0.9.0.1";
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", "3");
        props.put("batch.size", "16384");
        props.put("linger.ms", 1);
        props.put("buffer.memory", "33554432");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("message.max.bytes", "10MB");
        props.put("replica.fetch.max.bytes", "10MBB");
        props.put("max.block.ms", "15000");
        Thread.currentThread().setContextClassLoader(null);
        SimpleKafkaProducer kafkaProducer = new SimpleKafkaProducer(version,props);
        while(true) {
            System.out.println("result=" + kafkaProducer.send("test3", "7", "data"+new Random().nextInt(100)).getClass());
            Thread.sleep(new Random().nextInt(5000));
        }
        //logger.info("send success");


    }


}
