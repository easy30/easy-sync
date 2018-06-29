package com.cehome.easysync.objects;


import jsharp.util.DataMap;

public class Row {
    private String type;
    private String database;
    private String table;
    private Long timestamp;
    private Long serverId;

    private DataMap data=new DataMap();
    private DataMap oldData;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public DataMap getData() {
        return data;
    }

    public void setData(DataMap data) {
        this.data = data;
    }

    public DataMap getOldData() {
        return oldData;
    }

    public void setOldData(DataMap oldData) {
        this.oldData = oldData;
    }

}
