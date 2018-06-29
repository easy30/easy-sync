package com.cehome.easysync.objects.config;

public class MysqlConfig {

    private Mysql mysql;
    private Kafka kafka;

    public Mysql getMysql() {
        return mysql;
    }

    public void setMysql(Mysql mysql) {
        this.mysql = mysql;
    }

    public Kafka getKafka() {
        return kafka;
    }

    public void setKafka(Kafka kafka) {
        this.kafka = kafka;
    }
}
