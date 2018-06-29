package com.cehome.easysync;

import com.alibaba.fastjson.JSONObject;
import com.cehome.task.client.TimeTaskContext;
import com.cehome.task.client.TimeTaskPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;

@Component
public class DemoPlugin extends TimeTaskPlugin {
    private static final Logger logger = LoggerFactory.getLogger(DemoPlugin.class);
    @Override
    public void run(TimeTaskContext context, JSONObject args) throws Exception {
        logger.info("task id="+context.getId());
        logger.info("task name="+context.getName());
        logger.info("task run on ip="+ Inet4Address.getLocalHost().getHostAddress());
        logger.info("task sleep 2000");
        Thread.sleep(2000);
        logger.info("task run count="+context.getRunTimes());
    }

    @Override
    public void stop(TimeTaskContext context) throws Exception {
        logger.info("task "+context.getName()+" is stopped ");
    }
}
