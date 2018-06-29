package com.cehome.easysync.objects.config;

public class MysqlSource {
    private long timeTaskId;
    private String databases;
    private String tables;
    private String keys;
    private String keySep;

    public long getTimeTaskId() {
        return timeTaskId;
    }

    public void setTimeTaskId(long timeTaskId) {
        this.timeTaskId = timeTaskId;
    }

    public String getDatabases() {
        return databases;
    }

    public void setDatabases(String databases) {
        this.databases = databases;
    }

    public String getTables() {
        return tables;
    }

    public void setTables(String tables) {
        this.tables = tables;
    }

    public String getKeys() {
        return keys;
    }

    public void setKeys(String keys) {
        this.keys = keys;
    }

    public String getKeySep() {
        return keySep;
    }

    public void setKeySep(String keySep) {
        this.keySep = keySep;
    }
}
