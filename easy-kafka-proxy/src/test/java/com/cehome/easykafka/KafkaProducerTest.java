package com.cehome.easykafka;

import com.cehome.easykafka.producer.KafkaProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Properties;

/**
 * Created by houyanlin on 2018/06/26
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class KafkaProducerTest {
    Logger logger = LoggerFactory.getLogger(KafkaProducerTest.class);

    @Test
    public void sendTest() throws Exception{
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9094");
        props.put("acks", "all");
        props.put("retries", "3");
        props.put("batch.size", "16384");
        props.put("linger.ms", 1);
        props.put("buffer.memory", "33554432");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("message.max.bytes", "10MB");
        props.put("replica.fetch.max.bytes", "10MBB");
        KafkaProducer kafkaProducer = new KafkaProducer("0.10.1.0",props);
        kafkaProducer.send("test3","7","data5");
        logger.info("send success");
    }


}
