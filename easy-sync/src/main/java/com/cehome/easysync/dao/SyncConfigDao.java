package com.cehome.easysync.dao;

import com.cehome.easysync.domain.SyncConfig;
import jsharp.sql.SimpleDao;
import jsharp.support.BeanAnn;

//@Component
public class SyncConfigDao extends SimpleDao<SyncConfig>   {


    @Override
    public String getTableName() {
        BeanAnn beanAnn=  BeanAnn.getBeanAnn(SyncConfig.class);
        return beanAnn.getTable();
    }


    public  SyncConfig getByTimeTaskId(long timeTaskId){
        return this.queryOneByProps(null,"timeTaskId",timeTaskId);
    }


}
