package com.cehome.easykafka.consumer;

/**
 * Created by houyanlin on 2018/06/26
 **/
public class ConsumerRecord {

    private  String topic;
    private  int partition;
    private  long offset;
    private  long timestamp;
    private  int serializedKeySize;
    private  int serializedValueSize;
    private  String key;
    private  String value;

    public ConsumerRecord(String key,String value){
        this.key = key;
        this.value = value;
    }

    public ConsumerRecord(){

    }

    public ConsumerRecord(String topic,int partition,long offset,long timestamp,int serializedKeySize,int serializedValueSize,String key,String value){
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
        this.timestamp = timestamp;
        this.serializedKeySize = serializedKeySize;
        this.serializedValueSize = serializedValueSize;
        this.key = key;
        this.value = value;
    }


    public String topic() {
        return this.topic;
    }

    public int partition() {
        return this.partition;
    }


    public String key() {
        return this.key;
    }

    public String value() {
        return this.value;
    }

    public long offset() {
        return this.offset;
    }

    public long timestamp() {
        return this.timestamp;
    }


    public int serializedKeySize() {
        return this.serializedKeySize;
    }

    public int serializedValueSize() {
        return this.serializedValueSize;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ConsumerRecord{");
        sb.append("topic='").append(topic).append('\'');
        sb.append(", key='").append(key).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
