package com.cehome.easysync.dao;

import com.cehome.easysync.domain.Position;
import jsharp.sql.SimpleDao;
import jsharp.support.BeanAnn;

//@Component
public class PositionDao extends SimpleDao<Position>   {


    @Override
    public String getTableName() {
        BeanAnn beanAnn=  BeanAnn.getBeanAnn(PositionDao.class);
        return beanAnn.getTable();
    }


}
