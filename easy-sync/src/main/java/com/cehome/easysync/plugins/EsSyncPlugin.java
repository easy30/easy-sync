package com.cehome.easysync.plugins;

import com.alibaba.fastjson.JSONObject;
import com.cehome.easysync.Application;
import com.cehome.easysync.dao.SyncConfigDao;
import com.cehome.task.client.TimeTaskContext;
import com.cehome.task.client.TimeTaskPlugin;
import com.cehome.task.dao.TimeTaskDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;

@Component
public class EsSyncPlugin extends TimeTaskPlugin {
    private static final Logger logger = LoggerFactory.getLogger(EsSyncPlugin.class);
    @Autowired
    private SyncConfigDao syncConfigDao;
    @Autowired
    private TimeTaskDao timeTaskDao;
    @Override
    public void run(TimeTaskContext context, JSONObject args) throws Exception {
        logger.info("task id="+context.getId());
        logger.info("task name="+context.getName());
        logger.info("task run on ip="+ Inet4Address.getLocalHost().getHostAddress());
        logger.info("get es task config");
        EsSyncPluginRunner esSync= Application.getBean(EsSyncPluginRunner.class);
        esSync.run(context);

        logger.info("task run count="+context.getRunTimes());
    }

    @Override
    public void stop(TimeTaskContext context) throws Exception {
        logger.info("task "+context.getName()+" is stopped ");
    }
}
