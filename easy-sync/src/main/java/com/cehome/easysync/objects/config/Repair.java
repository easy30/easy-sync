package com.cehome.easysync.objects.config;

public class Repair {

    private String where;
    private int batchSize;
    private int batchInterval;

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getBatchInterval() {
        return batchInterval;
    }

    public void setBatchInterval(int batchInterval) {
        this.batchInterval = batchInterval;
    }
}
