package com.cehome.easykafka;

import com.cehome.easykafka.consumer.KafkaConsumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Properties;

/**
 * Created by houyanlin on 2018/06/26
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class KafkaConsumerTest {
    Logger logger = LoggerFactory.getLogger(KafkaConsumerTest.class);

    @Test
    public void pollTest() throws Exception{
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9094");
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
        KafkaConsumer kafkaConsumer = new KafkaConsumer("0.10.1.0",props);
        kafkaConsumer.createKafkaConsumer();
        String[] st = {"test3"};
        kafkaConsumer.subscribe(st);

        List result = kafkaConsumer.poll(5000);
        if (result.size() > 0){
            logger.info("record:{}",result.get(0));
        }
        logger.info("result:{}",result);
    }

}
