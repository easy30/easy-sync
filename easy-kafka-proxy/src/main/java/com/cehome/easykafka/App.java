package com.cehome.easykafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by houyanlin on 2018/06/21
 **/
@SpringBootApplication
@RestController
public class App {

    Logger logger = LoggerFactory.getLogger(App.class);

    @Value("${kafka.producer.bootstrapServers}")
    private String producerBootstrapServers;

    @Value("${kafka.producer.retries}")
    private String producerRetries;

    @Value("${kafka.producer.batchSize}")
    private String producerBatchSize;

    @Value("${kafka.producer.lingerMs}")
    private String producerLingerMs;

    @Value("${kafka.producer.bufferMemory}")
    private String producerBufferMemory;

    @Value("${kafka.consumer.bootstrapServers}")
    private String consumerBootstrapServers;
    @Value("${kafka.consumer.groupId}")
    private String consumerGroupId;

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(App.class, args);
    }

}
