package com.cehome.easysync.service;

import com.cehome.easysync.dao.SyncConfigDao;
import com.cehome.easysync.domain.SyncConfig;
import com.cehome.easysync.objects.TableTask;
import com.cehome.task.dao.TimeTaskDao;
import com.cehome.task.domain.TimeTask;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class TableTasksMapService {
    private static final Logger logger = LoggerFactory.getLogger(TableTasksMapService.class);

    @Autowired
    private SyncConfigDao syncConfigDao;
    @Autowired
    private TimeTaskDao timeTaskDao;
    @Cacheable(value = "tableTasksMap",key="#databaseService.timeTaskId")
    public Map<String, List<TableTask>> get(DatabaseService databaseService) {

        long mysqlTimeTaskId = databaseService.getTimeTaskId();
        List<SyncConfig> syncConfigs = syncConfigDao.queryListByProps(null, "mysqlTimeTaskId", mysqlTimeTaskId);
        Map<String, List<TableTask>> map = new HashMap<>();
        for (SyncConfig syncConfig : syncConfigs) {
            long timeTaskId = syncConfig.getTimeTaskId();
            TimeTask timeTask=timeTaskDao.get(timeTaskId);
            if(timeTask==null || timeTask.getStatus()==2){
                continue;
            }
            String[] tables = databaseService.getMatchTables(syncConfig.getMysqlDatabases(), syncConfig.getMysqlTables());
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
       return map;


    }

    // 清空  缓存
    @CacheEvict(value = "tableTasksMap",key="#mysqlTimeTaskId")
    public void clean(long mysqlTimeTaskId) {
        logger.info("clear cache for timetaskid=" + mysqlTimeTaskId);
    }
}
