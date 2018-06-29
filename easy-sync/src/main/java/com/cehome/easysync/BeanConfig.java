package com.cehome.easysync;

import com.cehome.easysync.dao.PositionDao;
import com.cehome.easysync.dao.SyncConfigDao;
import com.cehome.easysync.domain.Position;
import com.cehome.easysync.domain.SyncConfig;
import com.cehome.task.TimeTaskFactory;
import jsharp.sql.SessionFactory;
import jsharp.support.BeanAnn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class BeanConfig {
    @Autowired
    private TimeTaskFactory timeTaskFactory;
    @Bean
    public SessionFactory createSessionFactory(){
        return timeTaskFactory.getSessionFactory();
    }


    private  <T>T createDao(Class<T> dao,Class e,String name) throws Exception {
        BeanAnn beanAnn=  BeanAnn.getBeanAnn(e);
        String tableName=timeTaskFactory.getName()+"_"+name;
        beanAnn.setTable(tableName);
        T t= dao.newInstance();
        //positionDao.setTableName(tableName);
        //positionDao.setSessionFactory(timeTaskFactory.getSessionFactory());
        return t;
    }

    @Bean
    public PositionDao createPositionDao() throws Exception {

        return createDao(PositionDao.class, Position.class,"posi");

    }


    @Bean
    public SyncConfigDao createSyncConfigDao() throws Exception {

        return createDao(SyncConfigDao.class,SyncConfig.class,"sync_conf");

    }


    /**
     * https://www.cnblogs.com/imyijie/p/6518547.html
     * @return
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        List<Cache> caches=new ArrayList(){{
            add(new ConcurrentMapCache("tableTasksMap"));
        }};
        cacheManager.setCaches(caches);
        return cacheManager;
    }


}


