package com.cehome.easykafka;

/**
 * Created by houyanlin on 2018/06/25
 **/
public interface Consumer {

    public void createKafkaConsumer() throws Exception;

    public void subscribe(String... topic) throws Exception;


    public Object poll(long timeout);

    public void commitSync();

    public void close();

}
