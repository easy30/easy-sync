package com.cehome.easykafka;

/**
 * kafka生产者
 *
 * @author houyanlin
 * @create 2018-06-22 11:03
 **/
public interface Producer {

    public Object send(String topic,String key, String value) throws Exception;

    public void close() throws Exception;

}
