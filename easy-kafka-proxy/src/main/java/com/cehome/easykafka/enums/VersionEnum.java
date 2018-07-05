package com.cehome.easykafka.enums;

/**
 * kafka版本
 *
 * @author houyanlin
 * @create 2018-07-02 11:29
 **/
public enum VersionEnum {

    KAFKA_VERSION_8(8,"0.8","0.8.2.2"),
    KAFKA_VERSION_9(8,"0.9","0.9.0.1"),
    KAFKA_VERSION_10(8,"0.10","0.10.1.0"),
    KAFKA_VERSION_11(8,"0.11","0.11.0.2");

    private Integer code;
    private String value;
    private String desc;

    VersionEnum(Integer code,String value,String desc){
        this.code = code;
        this.value = value;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
