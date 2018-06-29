package com.cehome.easysync;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cehome.easysync.dao.SyncConfigDao;
import com.cehome.easysync.domain.SyncConfig;
import com.cehome.easysync.jest.Jest;
import com.cehome.easysync.objects.Column;
import com.cehome.easysync.objects.config.*;
import com.cehome.easysync.service.DatabaseService;
import com.cehome.easysync.utils.Const;
import com.cehome.easysync.utils.EsUtils;
import com.cehome.easysync.utils.Global;
import com.cehome.task.console.ClientServiceProxy;
import com.cehome.task.console.controller.TimeTaskController;
import com.cehome.task.dao.TimeTaskDao;
import com.cehome.task.domain.TimeTask;
import com.cehome.task.domain.TimeTaskSearch;
import io.searchbox.client.JestResult;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.mapping.PutMapping;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by ruixiang.mrx on 2017/9/6.
 */
@Controller
@RequestMapping("/timeTask")
public class DefaultTimeTaskController extends TimeTaskController {

    @Autowired
    private SyncConfigDao syncConfigDao;

    @Resource
    TimeTaskDao timetaskDao;

    @RequestMapping("list.htm")
    public String list(TimeTaskSearch timeTaskSearch, HttpServletRequest request,
                       HttpServletResponse response, Model model) throws Exception {
          super.list(timeTaskSearch,request,response,model);
          return "timeTask/list";
    }

    @RequestMapping("sourceList.htm")
    public String sourceList(TimeTaskSearch timeTaskSearch, HttpServletRequest request,
                       HttpServletResponse response, Model model) throws Exception {
        timeTaskSearch.setTaskType(Const.Task_TYPE_MYSQL);
        super.list(timeTaskSearch,request,response,model);
        return "timeTask/sourceList";
    }

    @RequestMapping("sourceEdit.htm")
    public String sourceEdit( HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
        edit(Const.Task_TYPE_MYSQL,request,response,model);
        return "timeTask/sourceEdit";
    }




    @RequestMapping("esList.htm")
    public String esList(TimeTaskSearch timeTaskSearch, HttpServletRequest request,
                             HttpServletResponse response, Model model) throws Exception {
        timeTaskSearch.setTaskType(Const.Task_TYPE_ES);
        super.list(timeTaskSearch,request,response,model);
        return "timeTask/esList";
    }

    @RequestMapping("esEdit.htm")
    public String esEdit( HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {

        List<TimeTask> mysqlSources = timetaskDao.queryList(" {taskType}=? and ( {status} in (0,1) )",Const.Task_TYPE_MYSQL);
        model.addAttribute("mysqlSources",mysqlSources);
        edit(Const.Task_TYPE_ES,request,response,model);
        return "timeTask/esEdit";
    }


    @ResponseBody
    @RequestMapping(value= "esAutoMapping.htm", produces="text/html;charset=UTF-8")
    public String esAutoMapping(String taskConfig, HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
        EsConfig esConfig=Global.toObject(taskConfig,EsConfig.class);

        long mysqlTimeTaskId = esConfig.getMysqlSource().getTimeTaskId();
        TimeTask mysqlTimeTask = timeTaskService.get(mysqlTimeTaskId);
        MysqlConfig  mysqlConfig = Global.toObject(mysqlTimeTask.getTaskConfig(), MysqlConfig.class);
        Mysql mysql = mysqlConfig.getMysql();

        DatabaseService  databaseService = DatabaseService.newInstance(mysql);
        MysqlSource mysqlSource=esConfig.getMysqlSource();
        EsField[] currentFields= esConfig.getEsMapping().getEsFields();
        Set<String> currentEsFieldSet=new HashSet<>();
        for(EsField esField :currentFields){
            currentEsFieldSet.add(esField.getSource().toLowerCase());
        }

        String[] table= databaseService.getFirstMatchTable(mysqlSource.getDatabases(),mysqlSource.getTables());

        List<Column> columns= databaseService.getColumns(table[0],table[1]);

        List<EsField> esFields=new ArrayList<>();
        boolean newVersion= esConfig.getEs().getVersion()>0;
        for(Column column :columns){

            if(currentEsFieldSet.contains(column.getName().toLowerCase())){
                continue;
            }

            EsField esField=new EsField();
            esField.setSource(column.getName());
            esField.setSourceType(column.getColumnType());
            esField.setTarget(column.getName());
            esField.setType(EsUtils.mysqlType2EsType(column.getDataType(),newVersion));
            esFields.add(esField);
        }

        return JSON.toJSONString(esFields);
    }


   /* @ResponseBody
    @RequestMapping("save.htm")
    public String save(HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
        int taskType= Convert.toInt(request.getParameter("taskType"),0);
        if(taskType==Const.Task_TYPE_ES){
            long id= Convert.toInt(request.getParameter("id"),0);
            if(id>0){
              TimeTask timeTask=  timeTaskService.get(id);
              timeTask.getTaskConfig()
            }

            String result = super.save(request, response, model);
            return result;

        }else {
            return  super.save(request, response, model);
        }


    }*/

    @Override
    protected void afterSave(TimeTask timeTask, HttpServletRequest request){
        if(timeTask.getTaskType()==Const.Task_TYPE_ES){
            SyncConfig syncConfig= syncConfigDao.queryOneByProps (null,"timeTaskId",timeTask.getId());
            if(syncConfig==null){
                syncConfig=syncConfigDao.createObject();
            }
            syncConfig.setTimeTaskId(timeTask.getId());
            String taskConfig=timeTask.getTaskConfig();
            EsConfig esConfig=esConfig = Global.toObject(taskConfig, EsConfig.class);

            MysqlSource mysqlSource=esConfig.getMysqlSource();
            Es es=esConfig.getEs();

            long mysqlTimeTaskId=esConfig.getMysqlSource().getTimeTaskId();

            boolean sameTables=mysqlSource.getDatabases().equals(syncConfig.getMysqlDatabases())
                    && mysqlSource.getTables().equals(syncConfig.getMysqlTables());

            syncConfig.setMysqlTimeTaskId(mysqlTimeTaskId);
            syncConfig.setMysqlDatabases(mysqlSource.getDatabases());
            syncConfig.setMysqlTables(mysqlSource.getTables());
            syncConfig.setMysqlKeys(mysqlSource.getKeys());
            syncConfig.setMysqlKeySep(mysqlSource.getKeySep());
            //syncConfig.setIndexName1("");//todo:
            syncConfigDao.save(syncConfig);

            if(!sameTables){

                ClientServiceProxy clientServiceProxy = timeTaskService.getClientServiceProxy(timeTaskService.get(mysqlTimeTaskId));
                logger.info("table changes, do cleanTableTasksMap cache");
                try {
                    clientServiceProxy.httpGet("remote/cleanTableTasksMap.htm?timeTaskId="+mysqlTimeTaskId);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }




        }
    }

    @ResponseBody
    @RequestMapping("esRebuildIndex.htm")
    public String esRebuildIndex(long id,HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
        TimeTask timeTask=timeTaskService.get(id);
        SyncConfig syncConfig= syncConfigDao.queryOneByProps (null,"timeTaskId",id);
        EsConfig esConfig = Global.toObject(timeTask.getTaskConfig(), EsConfig.class);
        String newIndexName=esConfig.getEs().getIndexAlias()+"_"+ DateFormatUtils.format(new Date(),"yyMMdd_HHmmss");
        Jest jest =new Jest(esConfig.getEs());

        boolean safe= esConfig.getRebuild().isSwitchAfterFullSync();

        //-- first build
        if(StringUtils.isBlank( syncConfig.getTaskConfig1()) || !safe){


            if(esConfig.getRebuild().isDeleteOldIndex() && StringUtils.isNotBlank(syncConfig.getWaitIndexName1())){
                if(!jest.deleteIndex(syncConfig.getWaitIndexName1())){
                    logger.error("delete old index "+syncConfig.getWaitIndexName1()+" fail.");
                }

            }

            syncConfig.setWaitIndexName1(newIndexName);
            syncConfig.setFullStatus1(Const.REBUILD_NEED);



        }else{
            //todo:delete old index , judge status not run first
            syncConfig.setWaitIndexName2(newIndexName);
            syncConfig.setFullStatus2(Const.REBUILD_NEED);
        }



      /*  String settings = "\"settings\" : {\n" +
                "        \"number_of_shards\" : 5,\n" +
                "        \"number_of_replicas\" : 1\n" +
                "    }\n"*/;

        logger.info("begin to create index {}",newIndexName);

       EsMapping esMapping= esConfig.getEsMapping();
        EsField[] esFields=esMapping.getEsFields();
        JSONObject  fields=new JSONObject();

        for(EsField esField:esFields){

            JSONObject  field=new JSONObject();
            field.put("type",esField.getType());
            if("text".equalsIgnoreCase(esField.getType())){
                field.put("analyzer", esConfig.getEs().getAnalyzer());
            }

            logger.debug("add custom config");
            if(StringUtils.isNotBlank(esField.getCustom())){
                field.putAll( Global.toObject( esField.getCustom()));
            }

            fields.put(esField.getTarget(),field);

        }

        JSONObject  mapping=new JSONObject();
        JSONObject  properties=new JSONObject();
        properties.put("properties",fields);



        final String type="doc";
        mapping.put(type,properties);

        PutMapping putMapping = new PutMapping.Builder(
                newIndexName,
                type,
                mapping.toJSONString()
        ).build();

        CreateIndex.Builder builder=new CreateIndex.Builder(newIndexName);
        if(StringUtils.isNotBlank(esConfig.getEs().getIndexSettings())){
            builder.settings(esConfig.getEs().getIndexSettings());
        }

        JestResult jestResult= jest.getJestClient().execute(builder.build());

        if(! jestResult.isSucceeded()){
            throw new Exception(jestResult.getErrorMessage());
        }

        jestResult= jest.getJestClient().execute(putMapping);

        if(! jestResult.isSucceeded()){
            throw new Exception(jestResult.getErrorMessage());
        }
        syncConfigDao.save(syncConfig);

        return newIndexName;


    }

    @ResponseBody
    @RequestMapping("esStopRebuildIndex.htm")
    public void esStopRebuildIndex(long id,HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
        TimeTask timeTask = timeTaskService.get(id);
        SyncConfig syncConfig = syncConfigDao.queryOneByProps(null, "timeTaskId", id);
        int status1=syncConfig.getFullStatus1();
        int status2=syncConfig.getFullStatus2();
        String message="";
        if(status2!=Const.REBUILD_NONE){
            syncConfig.setFullStatus2(Const.REBUILD_STOP);
            syncConfigDao.save(syncConfig);

        }else if(status1!=Const.REBUILD_NONE){
            syncConfig.setFullStatus1(Const.REBUILD_STOP);
            syncConfigDao.save(syncConfig);
        }


    }

    @ResponseBody
    @RequestMapping("esRepairData.htm")
    public void esRepairData(long id,HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
        TimeTask timeTask = timeTaskService.get(id);
        SyncConfig syncConfig = syncConfigDao.queryOneByProps(null, "timeTaskId", id);
        //EsConfig esConfig = Global.toObject(timeTask.getTaskConfig(), EsConfig.class);
        if (StringUtils.isBlank(syncConfig.getIndexName1())) {
            throw new Exception("Task not start , needn't fix");

        }

        syncConfig.setRepairStatus(Const.REBUILD_NEED);
        syncConfigDao.save(syncConfig);
    }

    @ResponseBody
    @RequestMapping("esStopRepairData.htm")
    public void esStopRepairData(long id,HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
        TimeTask timeTask = timeTaskService.get(id);
        SyncConfig syncConfig = syncConfigDao.queryOneByProps(null, "timeTaskId", id);
        syncConfig.setRepairStatus(Const.REBUILD_STOP);
        syncConfigDao.save(syncConfig);
    }



}
