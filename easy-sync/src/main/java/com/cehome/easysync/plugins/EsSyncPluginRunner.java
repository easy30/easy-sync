package com.cehome.easysync.plugins;

import com.cehome.easysync.dao.SyncConfigDao;
import com.cehome.easysync.domain.SyncConfig;
import com.cehome.easysync.jest.Jest;
import com.cehome.easysync.objects.config.*;
import com.cehome.easysync.service.DatabaseService;
import com.cehome.easysync.utils.Const;
import com.cehome.easysync.utils.Global;
import com.cehome.task.client.TimeTaskContext;
import com.cehome.task.dao.TimeTaskDao;
import com.cehome.task.domain.TimeTask;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
@Scope("prototype")
public class EsSyncPluginRunner {
    private static final Logger logger = LoggerFactory.getLogger(EsSyncPluginRunner.class);
    @Autowired
    private SyncConfigDao syncConfigDao;
    @Autowired
    private TimeTaskDao timeTaskDao;

    private TimeTaskContext context;
    String taskConfig;
    EsConfig esConfig;
    Es es;
    Jest jest;
    MysqlConfig mysqlConfig;
    Mysql mysql;
    Kafka kafka;
    //Map<String, EsSyncIncrement> esSyncIncrementMap = new HashMap<>();
    SyncConfig syncConfig;
    EsSyncFull esSyncFull1 = null;
    EsSyncFull esSyncFull2 = null;
    EsSyncFull esRepair = null;
    EsSyncIncrement esSyncIncrement1 = null;
    EsSyncIncrement esSyncIncrement2 = null;
    DatabaseService databaseService;
    private static int TIMEOUT = 3000;

    public EsSyncPluginRunner() throws Exception {

    }


    private EsSyncIncrement createIncrementTask(String indexName, String taskConfig) throws Exception {
        //if (StringUtils.isNotBlank(indexName)) {
        //EsSyncIncrement esSyncIncrement = esSyncIncrementMap.get(indexName);
        //if (esSyncIncrement == null) {
        logger.info("create increment task :" + indexName);
        EsSyncIncrement esSyncIncrement = new EsSyncIncrement(context, databaseService, kafka, indexName, jest, taskConfig);
        //esSyncIncrementMap.put(indexName, esSyncIncrement);
        esSyncIncrement.start();
        return esSyncIncrement;

        // }
        //}//
    }


    public void run(TimeTaskContext context) throws Exception {

        logger.info("get es task config");

        this.context = context;
        long timeTaskId = context.getId();
        TimeTask timeTask = timeTaskDao.get(timeTaskId);
        taskConfig = timeTask.getTaskConfig();
        esConfig = Global.toObject(taskConfig, EsConfig.class);
        es = esConfig.getEs();


        logger.info("get mysql task config and create DatabaseService");
        long mysqlTimeTaskId = esConfig.getMysqlSource().getTimeTaskId();
        TimeTask mysqlTimeTask = timeTaskDao.get(mysqlTimeTaskId);
        mysqlConfig = Global.toObject(mysqlTimeTask.getTaskConfig(), MysqlConfig.class);
        mysql = mysqlConfig.getMysql();
        kafka = mysqlConfig.getKafka();
        databaseService = DatabaseService.newInstance(mysql);

        logger.info("create es client with addresses=" + es.getAddresses());

        jest = new Jest(es);


        while (context.isRunning()) {
            try {
                //获取es_sync 记录
                syncConfig = syncConfigDao.queryOneByProps(null, "timeTaskId", timeTaskId);
                if (syncConfig != null) {

                    if (needRestart(timeTask)) return;

                    mainTask();
                    rebuildTask();
                    repairTask();
                } else {
                    logger.info("please save config first");
                }

                //logger.info("create increment task");


                context.sleep(5000);
            } catch (Exception e) {
                logger.error("", e);
                context.sleep(15000);
            }

        }


    }

    /**
     * when config changed, scheduler will auto restart.  so stop this turn.
     *
     * @param timeTask
     * @return
     */
    private boolean needRestart(TimeTask timeTask) {
        if (syncConfig.getFullStatus1() == Const.REBUILD_NEED || syncConfig.getFullStatus2() == Const.REBUILD_NEED
                || syncConfig.getRepairStatus() == Const.REBUILD_NEED) {

            TimeTask timeTask2 = timeTaskDao.get(timeTask.getId());
            if (Objects.equals(timeTask.getTaskConfig(), timeTask2.getTaskConfig())) {
                if (syncConfig.getFullStatus1() == Const.REBUILD_NEED) {
                    syncConfig.setFullStatus1(Const.REBUILD_ALLOW);
                }
                if (syncConfig.getFullStatus2() == Const.REBUILD_NEED) {
                    syncConfig.setFullStatus2(Const.REBUILD_ALLOW);
                }
                if (syncConfig.getRepairStatus() == Const.REBUILD_NEED) {
                    syncConfig.setRepairStatus(Const.REBUILD_ALLOW);
                }
                syncConfigDao.save(syncConfig);


            } else {
                logger.info("config changed. need restart task...");
                allStop();
                return true;

            }


        }

        return false;
    }

    private void mainTask() throws Exception {

        if (syncConfig.getFullStatus1() == Const.REBUILD_NONE) {
            if (esSyncIncrement1 == null) {
                if (StringUtils.isBlank(syncConfig.getIndexName1())) {
                    logger.info("[main] please click build/rebuild button first ");
                } else {
                    logger.info("[main] start increment task");
                    esSyncIncrement1 = createIncrementTask(syncConfig.getIndexName1(), syncConfig.getTaskConfig1());
                }
            }
        }

        if (syncConfig.getFullStatus1() == Const.REBUILD_ALLOW) {

            mainTaskAllow();

        }

        if (syncConfig.getFullStatus1() == Const.REBUILD_START) {
            mainTaskStart();
        }

        if (syncConfig.getFullStatus1() == Const.REBUILD_STOP) {
            logger.info("[main]force stop main task full sync ");
            syncConfig.setFullStatus1(Const.REBUILD_END);
            syncConfigDao.save(syncConfig);
        }

        if (syncConfig.getFullStatus1() == Const.REBUILD_END) {
            logger.info("[main]full sync task finished ");

            esSyncFull1Stop();

            bindAlias(es.getIndexAlias(), syncConfig.getIndexName1());

            syncConfig.setFullStatus1(Const.REBUILD_NONE);
            syncConfigDao.save(syncConfig);

        }

        //logger.info("-- end full sync task ");
    }

    private void mainTaskStart() throws Exception {
        if (esSyncIncrement1 == null) {
            esSyncIncrement1 = createIncrementTask(syncConfig.getIndexName1(), syncConfig.getTaskConfig1());
        }

        EsConfig esConfig1 = Global.toObject(syncConfig.getTaskConfig1(), EsConfig.class);
        if (esConfig1.getRebuild().isEnableFullSync()) {
            if (esSyncFull1 == null) {
                logger.info("[main]create full sync task");
                esSyncFull1 = new EsSyncFull(context, databaseService, syncConfigDao,
                        jest, 1);
                esSyncFull1.start();
            }

        } else {
            logger.info("[main]full sync is not enabled. change status to rebuild end");
            syncConfig.setFullStatus1(Const.REBUILD_END);
            syncConfigDao.save(syncConfig);
        }
    }

    private void mainTaskAllow() throws IOException {
        logger.info("[main] mainTask  build allow");


        String oldIndexName = syncConfig.getIndexName1();

        logger.info("[main] stop old  sync task: " + oldIndexName);
        esSyncIncrement1Stop();
        esSyncFull1Stop();


        if (StringUtils.isBlank(syncConfig.getTaskConfig1())) {
            logger.info("[main] first time to run");
        }

        syncConfig.setTaskConfig1(taskConfig);


        Rebuild rebuild = esConfig.getRebuild();
        boolean switchNow = !rebuild.isSwitchAfterFullSync();


        if (switchNow) {
            esRepairStop();
            esSyncIncrement2Stop();
            esSyncFull2Stop();


            bindAlias(es.getIndexAlias(), syncConfig.getWaitIndexName1());


            if (rebuild.isDeleteOldIndex() && StringUtils.isNotBlank(oldIndexName)) {
                logger.info("[main]delete old index " + oldIndexName);
                jest.deleteIndex(oldIndexName);
            }

            if (rebuild.isDeleteOldIndex() && StringUtils.isNotBlank(syncConfig.getIndexName2())) {
                jest.deleteIndex(syncConfig.getIndexName2());

            }
            syncConfig.setIndexName2("");
            syncConfig.setFullStatus2(Const.REBUILD_NONE);
            syncConfig.setRepairStatus(Const.REBUILD_NONE);


        } else {
            String alias = es.getIndexAlias();
            boolean sameAsAlias = jest.indexExists(alias);
            if (sameAsAlias) {
                logger.warn("[main] conflict, index name is same as alias name exists: {}, do not switch until after rebuild finish", alias);
            } else {
                logger.info("[main]switch index to {}", syncConfig.getWaitIndexName1());
                if (!jest.bindAliasAndIndex(es.getIndexAlias(), syncConfig.getWaitIndexName1())) {
                    throw new RuntimeException("can not switch index " + syncConfig.getWaitIndexName1() + " for alias {} " + es.getIndexAlias());
                }

            }


            if (rebuild.isDeleteOldIndex() && StringUtils.isNotBlank(oldIndexName)) {
                logger.info("[main]delete old index " + oldIndexName);
                jest.deleteIndex(oldIndexName);
            }


        }


        syncConfig.setIndexName1(syncConfig.getWaitIndexName1());
        syncConfig.setWaitIndexName1("");
        syncConfig.setFullTable1("");
        syncConfig.setFullPosition1(0);
        syncConfig.setFullStatus1(Const.REBUILD_START);
        syncConfigDao.save(syncConfig);
    }

    private void bindAlias(String alias, String index) throws IOException {
        //String alias=es.getIndexAlias();
        boolean sameAsAlias = jest.indexExists(alias);
        if (sameAsAlias) {
            logger.info("delete conflict index: " + alias);
            if (!jest.deleteIndex(alias)) {
                throw new IOException("can not delete index: " + alias);
            }
        }

        logger.info("switch index to {}", index);
        if (!jest.bindAliasAndIndex(alias, index)) {
            throw new IOException("can not switch index " + index + " for alias {} " + alias);
        }
    }

    private void rebuildTask() throws Exception {


        if (syncConfig.getFullStatus2() == Const.REBUILD_ALLOW) {
            logger.info("[rebuild] begin rebuild task");

            String indexName = syncConfig.getIndexName2();
            //EsSyncIncrement esSyncIncrement = esSyncIncrementMap.get(indexName);
            logger.info("[rebuild]remove old increment sync task: " + indexName);
            esSyncIncrement2Stop();

            logger.info("[rebuild]stop full sync");
            esSyncFull2Stop();


            EsConfig newEsConfig = Global.toObject(taskConfig, EsConfig.class);
            //EsConfig oldEsConfig=  Global.toObject(syncConfig.getTaskConfig2(),EsConfig.class);
            if (newEsConfig.getRebuild() != null && newEsConfig.getRebuild().isDeleteOldIndex()) {
                logger.info("[rebuild]delete index " + indexName);
                jest.deleteIndex(indexName);
            }


            syncConfig.setTaskConfig2(taskConfig);
            syncConfig.setIndexName2(syncConfig.getWaitIndexName2());
            syncConfig.setWaitIndexName2("");
            syncConfig.setFullTable2("");
            syncConfig.setFullPosition2(0);
            syncConfig.setFullStatus2(Const.REBUILD_START);
            syncConfigDao.save(syncConfig);
            logger.info("[rebuild] begin to rebuild ");


        }

        if (syncConfig.getFullStatus2() == Const.REBUILD_START) {

            if (esSyncIncrement2 == null) {
                esSyncIncrement2 = createIncrementTask(syncConfig.getIndexName2(), syncConfig.getTaskConfig2());
            }


            EsConfig esConfig2 = Global.toObject(syncConfig.getTaskConfig2(), EsConfig.class);
            if (esConfig2.getRebuild().isEnableFullSync()) {
                if (esSyncFull2 == null) {
                    esSyncFull2 = new EsSyncFull(context, databaseService, syncConfigDao,
                            jest, 2);
                    esSyncFull2.start();

                    //开启全量线程（全量同步结束后set rebuild =4）
                }

            } else {
                logger.info("[rebuild] full sync is not enabled. change status to rebuild end");
                syncConfig.setFullStatus2(Const.REBUILD_END);
                syncConfigDao.save(syncConfig);
            }


        }

        if (syncConfig.getFullStatus2() == Const.REBUILD_STOP) {
            logger.info("[rebuild] force stop rebuild task");
            esSyncIncrement2Stop();
            esSyncFull2Stop();
            syncConfig.setFullStatus2(Const.REBUILD_NONE);
            ;
            syncConfigDao.save(syncConfig);
        }


        if (syncConfig.getFullStatus2() == Const.REBUILD_END) {
            if (StringUtils.isNotBlank(syncConfig.getIndexName2())) {

                logger.info("[rebuild] switch index for alias " + es.getIndexAlias());
                bindAlias(es.getIndexAlias(), syncConfig.getIndexName2());


                if (Global.toObject(syncConfig.getTaskConfig2(), EsConfig.class).getRebuild().isDeleteOldIndex()) {
                    if (Objects.equals(syncConfig.getIndexName1(), syncConfig.getIndexName2())) {
                        logger.warn("[rebuild] old index same as new index : {} , ignore ", syncConfig.getIndexName1());

                    } else {
                        logger.info("[rebuild] delete old index :" + syncConfig.getIndexName1());
                        jest.deleteIndex(syncConfig.getIndexName1());
                    }
                }


                switchTasks();
                logger.info("[rebuild] rebuild task finished");


            }


        }
    }

    private void repairTask() throws Exception {

        if (syncConfig.getRepairStatus() == Const.REBUILD_ALLOW) {
            esRepairStop();

            syncConfig.setRepairTaskConfig(taskConfig);
            syncConfig.setRepairTable("");
            syncConfig.setRepairPosition(0);
            syncConfig.setRepairStatus(Const.REBUILD_START);
            syncConfigDao.save(syncConfig);

        }
        if (syncConfig.getRepairStatus() == Const.REBUILD_START) {
            if (esRepair == null) {
                logger.info("[repair] create repair task");
                esRepair = new EsSyncFull(context, databaseService, syncConfigDao,
                        jest, 3);
                esRepair.start();
            }
        }

        if (syncConfig.getRepairStatus() == Const.REBUILD_STOP) {
            logger.info("[repair] repair task stop ");
            syncConfig.setRepairStatus(Const.REBUILD_END);
            syncConfigDao.save(syncConfig);
        }

        if (syncConfig.getRepairStatus() == Const.REBUILD_END) {
            logger.info("[repair] repair task end ");
            esRepairStop();
            syncConfig.setRepairStatus(Const.REBUILD_NONE);
            syncConfigDao.save(syncConfig);

        }


    }




/*
    private boolean switchIndex(String oldIndex,String newIndex) throws IOException {


        List<AliasMapping> list=new ArrayList<>();
        if(StringUtils.isNotBlank(oldIndex)) {
            RemoveAliasMapping removeAliasMapping = new RemoveAliasMapping.Builder(oldIndex, es.getIndexAlias()).build();
            list.add(removeAliasMapping);
        }
        AddAliasMapping addAliasMapping = new AddAliasMapping.Builder(newIndex, es.getIndexAlias()).build();
        list.add(addAliasMapping);
        ModifyAliases modifyAliases = new ModifyAliases.Builder(list).build();
        JestResult jr = jest.execute(modifyAliases);
        if (jr.isSucceeded()) {
            return true;

        } else {
            logger.error("modifyAliases  error :" + jr.getErrorMessage());
            return false;
        }
    }
*/


    private void switchTasks() {


        esSyncFull1Stop();

        esSyncFull2Stop();

        syncConfig.setFullStatus1(Const.REBUILD_NONE);// if exists
        syncConfig.setFullStatus2(Const.REBUILD_NONE);


        esRepairStop();

        syncConfig.setRepairStatus(Const.REBUILD_NONE);

        esSyncIncrement1Stop();
        //logger.info("replace old incr with new incr");
        esSyncIncrement1 = esSyncIncrement2;
        esSyncIncrement2 = null;

        syncConfig.setIndexName1(syncConfig.getIndexName2());
        syncConfig.setIndexName2("");
        syncConfig.setTaskConfig1(syncConfig.getTaskConfig2());

        syncConfigDao.save(syncConfig);


    }

    private void esSyncFull1Stop() {
        if (esSyncFull1 != null) {
            esSyncFull1.terminate(TIMEOUT);
            esSyncFull1 = null;
        }
    }

    private void esSyncFull2Stop() {
        if (esSyncFull2 != null) {
            esSyncFull2.terminate(TIMEOUT);
            esSyncFull2 = null;
        }
    }

    private void esRepairStop() {
        if (esRepair != null) {
            logger.info("stop repair sync");
            esRepair.terminate(TIMEOUT);
            esRepair = null;
        }
    }

    private void esSyncIncrement1Stop() {
        if (esSyncIncrement1 != null) {
            esSyncIncrement1.terminate(TIMEOUT);
            esSyncIncrement1 = null;
        }
    }

    private void esSyncIncrement2Stop() {
        if (esSyncIncrement2 != null) {
            esSyncIncrement2.terminate(TIMEOUT);
            esSyncIncrement2 = null;
        }
    }

    private void allStop() {
        logger.info("stop all");
        if (esSyncFull1 != null) esSyncFull1.terminate(0);
        if (esSyncFull2 != null) esSyncFull2.terminate(0);
        if (esRepair != null) esRepair.terminate(0);
        if (esSyncIncrement1 != null) esSyncIncrement1.terminate(0);
        if (esSyncIncrement2 != null) esSyncIncrement2.terminate(0);
        context.sleep(TIMEOUT);
    }


}
