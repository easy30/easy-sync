package com.cehome.easysync.domain;

import jsharp.sql.BaseDO;
import jsharp.sql.anno.Table;

import javax.persistence.Id;

@Table
public class SyncConfig extends BaseDO {

    @Id
    private long id;

    private long timeTaskId;

    //can not modify
    private long mysqlTimeTaskId;

    //
    private String mysqlDatabases;
    private String mysqlTables;
    private String mysqlKeys;
    private String mysqlKeySep;


    private String indexName1;
    private String waitIndexName1;
    private String taskConfig1;
    private String fullTable1;
    private long fullPosition1;
    private int fullStatus1;

    private String indexName2;
    private String waitIndexName2;
    private String taskConfig2;
    private String fullTable2;
    private long fullPosition2;
    private int fullStatus2;

    private String repairTaskConfig;
    private String repairTable;
    private long repairPosition;
    private int repairStatus;


    //private String rebuildTables;
   /* private String rebuildTable;

    private long rebuildPosition;

    private int rebuildStatus;*/

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimeTaskId() {
        return timeTaskId;
    }

    public void setTimeTaskId(long timeTaskId) {
        this.timeTaskId = timeTaskId;
    }

    public long getMysqlTimeTaskId() {
        return mysqlTimeTaskId;
    }

    public void setMysqlTimeTaskId(long mysqlTimeTaskId) {
        this.mysqlTimeTaskId = mysqlTimeTaskId;
    }

    public String getMysqlDatabases() {
        return mysqlDatabases;
    }

    public void setMysqlDatabases(String mysqlDatabases) {
        this.mysqlDatabases = mysqlDatabases;
    }

    public String getMysqlTables() {
        return mysqlTables;
    }

    public void setMysqlTables(String mysqlTables) {
        this.mysqlTables = mysqlTables;
    }

    public String getMysqlKeys() {
        return mysqlKeys;
    }

    public void setMysqlKeys(String mysqlKeys) {
        this.mysqlKeys = mysqlKeys;
    }

    public String getMysqlKeySep() {
        return mysqlKeySep;
    }

    public void setMysqlKeySep(String mysqlKeySep) {
        this.mysqlKeySep = mysqlKeySep;
    }

    public String getIndexName1() {
        return indexName1;
    }

    public void setIndexName1(String indexName1) {
        this.indexName1 = indexName1;
    }

    public String getTaskConfig1() {
        return taskConfig1;
    }

    public void setTaskConfig1(String taskConfig1) {
        this.taskConfig1 = taskConfig1;
    }

    public String getFullTable1() {
        return fullTable1;
    }

    public void setFullTable1(String fullTable1) {
        this.fullTable1 = fullTable1;
    }

    public long getFullPosition1() {
        return fullPosition1;
    }

    public void setFullPosition1(long fullPosition1) {
        this.fullPosition1 = fullPosition1;
    }

    public int getFullStatus1() {
        return fullStatus1;
    }

    public void setFullStatus1(int fullStatus1) {
        this.fullStatus1 = fullStatus1;
    }

    public String getIndexName2() {
        return indexName2;
    }

    public void setIndexName2(String indexName2) {
        this.indexName2 = indexName2;
    }

    public String getTaskConfig2() {
        return taskConfig2;
    }

    public void setTaskConfig2(String taskConfig2) {
        this.taskConfig2 = taskConfig2;
    }

    public String getFullTable2() {
        return fullTable2;
    }

    public void setFullTable2(String fullTable2) {
        this.fullTable2 = fullTable2;
    }

    public long getFullPosition2() {
        return fullPosition2;
    }

    public void setFullPosition2(long fullPosition2) {
        this.fullPosition2 = fullPosition2;
    }

    public int getFullStatus2() {
        return fullStatus2;
    }

    public void setFullStatus2(int fullStatus2) {
        this.fullStatus2 = fullStatus2;
    }

    public String getWaitIndexName1() {
        return waitIndexName1;
    }

    public void setWaitIndexName1(String waitIndexName1) {
        this.waitIndexName1 = waitIndexName1;
    }

    public String getWaitIndexName2() {
        return waitIndexName2;
    }

    public void setWaitIndexName2(String waitIndexName2) {
        this.waitIndexName2 = waitIndexName2;
    }

    public String getRepairTaskConfig() {
        return repairTaskConfig;
    }

    public void setRepairTaskConfig(String repairTaskConfig) {
        this.repairTaskConfig = repairTaskConfig;
    }

    public String getRepairTable() {
        return repairTable;
    }

    public void setRepairTable(String repairTable) {
        this.repairTable = repairTable;
    }

    public long getRepairPosition() {
        return repairPosition;
    }

    public void setRepairPosition(long repairPosition) {
        this.repairPosition = repairPosition;
    }

    public int getRepairStatus() {
        return repairStatus;
    }

    public void setRepairStatus(int repairStatus) {
        this.repairStatus = repairStatus;
    }
}
