package com.cehome.easykafka;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.cehome.easykafka.consumer.SimpleKafkaConsumer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

/**
 * Created by houyanlin on 2018/06/26
 **/
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = App.class)
public class KafkaConsumerTest {
    static Logger logger = LoggerFactory.getLogger(KafkaConsumerTest.class);
    static {
        ( (LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("ROOT").setLevel(Level.INFO);
    }

    @Test
    public void pollTest() throws Exception{
        String version="0.11.0.1";
        //version="0.9.0.1";
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id","test1");
        props.put("acks", "all");
        props.put("retries", "3");
        props.put("batch.size", "16384");
        props.put("linger.ms", 1);
        props.put("buffer.memory", "33554432");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
//        props.put("partition.assignment.strategy","range");
        props.put("message.max.bytes", "10MB");
        props.put("replica.fetch.max.bytes", "10MBB");
        Thread.currentThread().setContextClassLoader(null);
        SimpleKafkaConsumer kafkaConsumer = new SimpleKafkaConsumer(version,props);
        kafkaConsumer.createKafkaConsumer();
        String[] st = {"test3"};
        kafkaConsumer.subscribe(st);
        while (true) {
            List result = kafkaConsumer.poll(5000);
            if (result.size() > 0) {
                logger.info("record:{}", result.get(0));
            }
            logger.info("result:{}", result);
        }
    }

}
