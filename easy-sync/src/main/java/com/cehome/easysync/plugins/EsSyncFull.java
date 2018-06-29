package com.cehome.easysync.plugins;

import com.alibaba.fastjson.JSON;
import com.cehome.easysync.dao.SyncConfigDao;
import com.cehome.easysync.domain.SyncConfig;
import com.cehome.easysync.jest.Jest;
import com.cehome.easysync.objects.Column;
import com.cehome.easysync.objects.config.EsConfig;
import com.cehome.easysync.objects.config.EsField;
import com.cehome.easysync.objects.config.Rebuild;
import com.cehome.easysync.objects.config.Repair;
import com.cehome.easysync.service.DatabaseService;
import com.cehome.easysync.utils.Const;
import com.cehome.easysync.utils.Global;
import com.cehome.task.client.TimeTaskContext;
import io.searchbox.core.Bulk;
import io.searchbox.core.BulkResult;
import io.searchbox.core.Index;
import jsharp.util.DataMap;
import jsharp.util.TimeCal;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class EsSyncFull extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(EsSyncFull.class);

    private TimeTaskContext context;
    DatabaseService databaseService;
    SyncConfigDao syncConfigDao;
    Jest jest;
    String indexName;
    int kind;
    private EsConfig esConfig;
    private EsField[] esFields;
    //private Rebuild rebuild;
    Map<String,Column> columnMap;
    private volatile boolean running=true;
    TimeCal timeCal=new TimeCal(5);
    public EsSyncFull(TimeTaskContext context, DatabaseService databaseService,SyncConfigDao syncConfigDao,
                      Jest jest,int kind){
        this.context=context;
        this.databaseService= databaseService;
        this.syncConfigDao= syncConfigDao;
        this.jest=jest;
        this.kind=kind;

    }

    static  class SyncConfigProxy{
        SyncConfig syncConfig;
        int kind;

        SyncConfigProxy(SyncConfig syncConfig ,int kind){
            this.syncConfig=syncConfig;
            this.kind=kind;
        }

        public String getIndexName(){
            return  kind==1 || kind==3 ? syncConfig.getIndexName1() : syncConfig.getIndexName2();
        }
        public EsConfig getEsConfig(){
            String taskConfig= kind==1 ? syncConfig.getTaskConfig1():kind==2?syncConfig.getTaskConfig2():syncConfig.getRepairTaskConfig();
            return Global.toObject(taskConfig, EsConfig.class);
        }
        public String getTable(){
           return kind==1 ? syncConfig.getFullTable1() : kind==2 ? syncConfig.getFullTable2(): syncConfig.getRepairTable();
        }

        public long getPosition(){
            if (kind==1) {
                return syncConfig.getFullPosition1();

            } else if(kind==2){
                return syncConfig.getFullPosition2();

            }else{
               return syncConfig.getRepairPosition();

            }
        }
        public void setPosition(long posi){
            if (kind==1) {
                syncConfig.setFullPosition1(posi);
            } else if (kind==2){
                syncConfig.setFullPosition2(posi);
            }else{
                syncConfig.setRepairPosition(posi);
            }
        }

        public void setTable(String table ){
            if (kind==1) {

                syncConfig.setFullTable1(table);
            } else if(kind==2){

                syncConfig.setFullTable2(table);
            }else{

                syncConfig.setRepairTable(table);
            }
        }

        public void setStatus(int status){
            if (kind==1) {
                logger.info("main task full sync finished");
                syncConfig.setFullStatus1(Const.REBUILD_END);
            } else if(kind==2) {
                logger.info("rebuild task full sync finished");
                syncConfig.setFullStatus2(Const.REBUILD_END);
            }else{
                logger.info("repair task finished");
                syncConfig.setRepairStatus(Const.REBUILD_END);
            }
        }


    }

    private boolean canRun(){
       return context.isRunning() && running;
    }
    @Override
    public void run() {
        boolean finished = false;
        SyncConfig syncConfig =null;
        SyncConfigProxy syncConfigProxy=null;
        long count=0;
        long beginTime=System.currentTimeMillis();

        MDC.put("shard", "task/"+context.getId());
        while (canRun()) {
            try {
                long timeTaskId = context.getId();
                syncConfig = syncConfigDao.getByTimeTaskId(timeTaskId);
                syncConfigProxy=new SyncConfigProxy(syncConfig,kind);
                indexName =syncConfigProxy.getIndexName();
                esConfig = syncConfigProxy.getEsConfig();

                esFields= esConfig.getEsMapping().getEsFields();

                int limit=20;
                int interval=0;
                String where="";
                if(kind==1 || kind==2) {
                    Rebuild rebuild = esConfig.getRebuild();
                    if (rebuild.getFullBatchSize() > 0) limit = rebuild.getFullBatchSize();
                    interval = rebuild.getFullBatchInterval();
                    where = rebuild.getFullWhere();
                }else{
                    Repair repair=esConfig.getRepair();
                    if(repair.getBatchSize()>0) limit=repair.getBatchSize();
                    interval=repair.getBatchInterval();
                    where=repair.getWhere();
                }

                String currentTable =syncConfigProxy.getTable();
                String[] tables = databaseService.getMatchTables(syncConfig.getMysqlDatabases(), syncConfig.getMysqlTables());
                if(ArrayUtils.isEmpty(tables)){
                   logger.warn("not match tables for "+syncConfig.getMysqlDatabases()+" and "+syncConfig.getMysqlTables());
                   finished=true;
                   break;
                }
                String[] dt=tables[0].split("\\.");
                columnMap=databaseService.getColumnMap(dt[0],dt[1]);

                //JSONArray tables= JSON.parseArray(syncConfig.getRebuildTables());
                int tableIndex = getTableIndex(tables, currentTable);


                if (tableIndex >= 0) {

                    for (int i = tableIndex; i < tables.length; i++) {
                        //JSONArray tableInfo = tables.getJSONArray(i);

                        String table = tables[i];// tableInfo.getString(0);
                        //long max = tableInfo.getLongValue(1);
                        logger.info("begin to full sync with table {}", table);
                        String pk = syncConfig.getMysqlKeys();
                        long posi=syncConfigProxy.getPosition();
                        syncConfigProxy.setTable(table);
                        syncConfigDao.save(syncConfig);

                        //if(max>min) {
                        while (canRun()) {
                            try {
                                List<DataMap> list = databaseService.queryList(table,where, pk, posi, limit);
                                if (list.size() == 0) {
                                    if(i==tables.length-1){
                                        finished = true;
                                    }
                                    break;
                                } else {
                                    count+=list.size();
                                    posi = doSync(list, table,pk);

                                    long cost=System.currentTimeMillis()-beginTime;
                                    logger.info("count={}, cost={}, avg={}",count,cost,cost/count);

                                    //logger.info("update position to " + posi);
                                    syncConfigProxy.setPosition(posi);

                                    if(timeCal.isTimeUp()>0) {
                                        syncConfigDao.save(syncConfig);
                                    }

                                    context.sleep(interval);
                                }
                            } catch (Exception e) {
                                logger.error("", e);
                                context.sleep(3000);
                            }

                        }
                        //}

                        if (!canRun()) break;


                    }


                }


            } catch (Exception e) {
                logger.error("", e);
            }
            finally {
                if (finished) {

                     syncConfigProxy.setStatus(Const.REBUILD_END);

                    syncConfigDao.save(syncConfig);
                    break;
                }

            }
        }

        MDC.remove("shard");


    }

    public void terminate(long waitMS)   {
        running=false;
        if(waitMS>0) {
            try {
                this.join(waitMS);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
        }
    }


    private long doSync( List<DataMap> list,String table,String pk) throws IOException {
        long posi=0;
        //List<Map> list2=new ArrayList<>();
        Bulk.Builder bulk = new Bulk.Builder().defaultIndex(indexName).defaultType(Const.ES_TYPE);
        for(DataMap dataMap:list){
            long id=dataMap.getLong(pk);
            if(id>posi) posi=id;
            Map map=(convertMysql2Es(dataMap));
            logger.info("[full] {}={}",table,map);
            Index index = new Index.Builder(map).id(""+id).build();
            bulk.addAction(index);

        }


        BulkResult br = jest.getJestClient().execute(bulk.build());
        if(!br.isSucceeded()){
            logger.error("error={}, failItems={}",br.getErrorMessage(), JSON.toJSONString(br.getFailedItems()));
            //   br.getFailedItems().get(0).
            throw new RuntimeException("bulk error");
        }
        return  posi;


    }

    private Map convertMysql2Es(DataMap dataMap) throws UnsupportedEncodingException {
        DataMap result=new DataMap();
        for(EsField esField :esFields){
            String name=esField.getSource();
            String targetType=esField.getType();
            Column column=columnMap.get(name);
            Object value=dataMap.get(name);
            if(value!=null) {
                if ("boolean".equalsIgnoreCase(targetType)) {
                    value = ConvertUtils.convert(value, Boolean.class);
                } else if ("date".equalsIgnoreCase(targetType)) {
                    if (value instanceof Date) {
                        value = ((Date) value).getTime();
                    }
                } else if ("binary".equalsIgnoreCase(targetType)) {
                    if (value instanceof byte[]) {
                        value = org.apache.commons.codec.binary.Base64.encodeBase64String((byte[]) value);
                    } else {
                        value = org.apache.commons.codec.binary.Base64.encodeBase64String(value.toString().getBytes("UTF-8"));
                    }
                }
            }

             result.put(esField.getTarget(),value);


        }


        return result;
    }

    private int getTableIndex( String[] tables, String table){
        if(tables==null ||tables.length==0) {
            logger.error("no match tables found");
            return -1;
        }
        if(StringUtils.isBlank(table)) return  0;
        for(int i=0;i<tables.length;i++){
            if(tables[i].equals(table)){
                return i;
            }
        }
        logger.error("can not find table:"+table);
        return -1;
    }
}
