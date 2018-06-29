package com.cehome.easysync.service;

import com.alibaba.fastjson.JSONArray;
import com.cehome.easysync.Application;
import com.cehome.easysync.dao.SyncConfigDao;
import com.cehome.easysync.objects.Column;
import com.cehome.easysync.objects.config.Mysql;
import com.cehome.easysync.utils.CharsetConversion;
import jsharp.sql.ObjectSessionFactory;
import jsharp.sql.SessionFactory;
import jsharp.util.Common;
import jsharp.util.DataMap;
import jsharp.util.DataValue;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.sql.*;
import java.util.*;

@Service
@Scope("prototype")
public class DatabaseService {

    private static String driverClassName = "com.mysql.jdbc.Driver";
    DataSource dataSource;
    SessionFactory sessionFactory;
    private long timeTaskId;
    private long serverId;

    //private volatile Map<String, List<TableTask>> tableTasksMap = null;
    @Autowired
    private SyncConfigDao syncConfigDao;

    private ConcurrentMapCache columnsCache=new ConcurrentMapCache("columns");
    private ConcurrentMapCache columnMapCache=new ConcurrentMapCache("columnMap");


    protected DataSource createDataSource(Mysql mysql) {

        DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(mysql.getUrl());
        dataSource.setUsername(mysql.getUser());
        dataSource.setPassword(mysql.getPassword());
        dataSource.setValidationQuery("select 1");
        dataSource.setTestWhileIdle(true);
        dataSource.setTimeBetweenEvictionRunsMillis(30*1000);
        return dataSource;
    }

    public void setMysql(Mysql mysql) {
        dataSource = createDataSource(mysql);
        sessionFactory = new ObjectSessionFactory();
        sessionFactory.setDataSource(dataSource);

        DataValue dataValue = sessionFactory.queryValue("SELECT @@server_id as server_id");
        //if(dataValue==null) throw new JSException("server id is null");
        serverId = dataValue.getLong(-1);

    }

    public static DatabaseService newInstance(Mysql mysql) {

        DatabaseService databaseService = Application.getBean(DatabaseService.class);
        databaseService.setMysql(mysql);
        return databaseService;
    }


    public long getTimeTaskId() {
        return timeTaskId;
    }

    public void setTimeTaskId(long timeTaskId) {
        this.timeTaskId = timeTaskId;
    }

    public long getServerId() {
        return this.serverId;

    }

    /*public synchronized List<TableTask> getTableTasks(String database, String table) {

        if (tableTasksMap == null) {

            long mysqlTimeTaskId = getTimeTaskId();
            List<SyncConfig> syncConfigs = syncConfigDao.queryListByProps(null, "mysqlTimeTaskId", mysqlTimeTaskId);
            Map<String, List<TableTask>> map = new HashMap<>();
            for (SyncConfig syncConfig : syncConfigs) {
                long timeTaskId = syncConfig.getTimeTaskId();
                String[] tables = getMatchTables(syncConfig.getMysqlDatabases(), syncConfig.getMysqlTables(), syncConfig.getMysqlKeys());
                if (ArrayUtils.isNotEmpty(tables)) {
                    for (String t : tables) {
                        List<TableTask> tableTasks = map.get(t);
                        if (tableTasks == null) {
                            tableTasks = new ArrayList<>();
                            map.put(t, tableTasks);
                        }
                        TableTask tableTask=new TableTask();
                        tableTask.setTimeTaskId(timeTaskId);
                        tableTask.setKeys(syncConfig.getMysqlKeys());
                        tableTasks.add(tableTask);

                    }
                }

            }
            tableTasksMap = map;
        }


        return tableTasksMap.get(database+"."+table);

    }

    public synchronized void clearTableTasks() {
        tableTasksMap = null;
    }*/


    public String[] getMatchTables(String databases, String tables) {

        return getMatchTables(databases,tables,0);
    }
    public String[] getFirstMatchTable(String databases, String tables) {
        String[] ss=getMatchTables(databases,tables,1);
        if(ss.length>0) return  ss[0].split("\\.");
        return new String[0];
    }
    private String[] getMatchTables(String databases, String tables,int limit) {
        String oper1=databases.indexOf('%')>=0?"like":"=";
        String oper2=tables.indexOf('%')>=0?"like":"=";
        String sql = "select table_schema,table_name from information_schema.tables where ";
        sql+=parseValues("table_name",tables);
        if(StringUtils.isNotBlank(databases)){
            sql+=" and "+parseValues("table_schema",databases);
        }

        sql+="  order by table_schema,table_name";

        if(limit>0){
            sql+="  limit "+limit+" ";
        }

        List<DataMap> list = sessionFactory.queryList(sql);
        String[] result = new String[list.size()];

        int i = 0;
        for (DataMap dataMap : list) {
            String tableName = dataMap.getString("table_schema") + "." + dataMap.getString("table_name");
            result[i++] = tableName;
        }
        return result;


    }

    private String parseValues(String field,String values){

        if(values.indexOf(',')>=-1){
            String oper=values.indexOf('%')>=0?" like ":" = ";
            return " ( "+field+oper +" '"+values+"' ) ";
        }else{
            // a='a' or b='b' or c like 'c%'
             StringBuilder sb=new StringBuilder();
            String[] ss=values.split(",");
            for(String s :ss){
                if(sb.length()>0) sb.append( " or ");
                String oper=s.indexOf('%')>=0?" like ":" = ";
                sb.append( field +oper +" '"+s+"' ");
            }
            return " (" +sb.toString()+") ";
        }
    }

    public JSONArray getMatchTables2(String databases, String tables, String pk) {
        String oper1=databases.indexOf('%')>=0?"like":"=";
        String oper2=tables.indexOf('%')>=0?"like":"=";
        String sql = "select table_schema,table_name from information_schema.tables where table_schema "+oper1
                +" ? and table_name "+oper2+" ? order by table_schema,table_name";
        List<DataMap> list = sessionFactory.queryList(sql, databases, tables);
        JSONArray result = new JSONArray(list.size());

        for (DataMap dataMap : list) {
            String tableName = dataMap.getString("table_schema") + "." + dataMap.getString("table_name");
            DataValue dataValue = sessionFactory.queryValue("select max(" + pk + ") from " + tableName);
            long max = dataValue.getLong(0);
            JSONArray row = new JSONArray();
            row.add(tableName);
            row.add(max);
            result.add(row);


        }
        return result;
        //

    }



    public List<Column> getColumns(String database, String table) throws SQLException {

        String key=database+"."+table;
        List<Column>  list=  columnsCache.get(key, List.class);
        if(list==null) {


            String sql = "select column_name name,data_type dataType , column_type columnType, character_set_name charset  from information_schema.columns " +

                    "where table_name=? and table_schema=? order by ordinal_position";
            list = sessionFactory.queryList(sql, Column.class, table, database);
            for (Column column : list) {
                if (StringUtils.isNotBlank(column.getCharset())) {
                    column.setCharset(CharsetConversion.getJavaCharset(column.getCharset()));
                }
            }
            columnsCache.putIfAbsent(key,list);
        }

        return list;

    }

    public Map<String,Column> getColumnMap(String database, String table) throws SQLException {
        String key=database+"."+table;
        Map<String,Column> map=columnMapCache.get(key,Map.class);
        if(map==null) {
            List<Column> list = getColumns(database, table);
            map=new LinkedCaseInsensitiveMap();
            for(Column column :list){
                map.put(column.getName(),column);
            }
            columnMapCache.putIfAbsent(key,map);
        }
        return map;
    }

    @Deprecated
    public Column[] getColumns2(String database, String table) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            String sql = "select * from " + database + "." + table + " limit 1 ";

            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            ResultSetMetaData data = rs.getMetaData();
            Column[] columns = new Column[data.getColumnCount()];
            for (int i = 1; i <= data.getColumnCount(); i++) {
                Column column = new Column();
                column.setName(data.getColumnName(i));
                //column.setType(data.getColumnType(i));
                columns[i - 1] = column;
            }
            return columns;

        } finally {
            Common.closeObjects(rs, stmt, conn);

        }
    }

    public List<DataMap> queryList(String table, String where, String pk, long min ,int limit) {
        if(StringUtils.isNotBlank(where)) {
            where=" ( "+where+" ) and ";
        }
        if(where==null) where="";
        String sql = String.format("select * from %s where %s %s > ?  order by %s limit ? ", table, where, pk, pk);
        List<DataMap> list = sessionFactory.queryList(sql, min, limit);
        return list;
    }


    public int getTimezoneHours(){
        String sql="SELECT EXTRACT(HOUR FROM (TIMEDIFF(NOW(), UTC_TIMESTAMP))) AS `timezone`";
        return sessionFactory.queryValue(sql).getInt(0);

    }

}
