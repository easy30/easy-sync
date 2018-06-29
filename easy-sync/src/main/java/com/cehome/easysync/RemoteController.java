package com.cehome.easysync;

import com.cehome.easysync.service.TableTasksMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/remote")
public class RemoteController {
    @Autowired
    TableTasksMapService tableTasksMapService;

    @RequestMapping("cleanTableTasksMap.htm")
    public void cleanTableTasksMap(long timeTaskId){
        tableTasksMapService.clean(timeTaskId);

    }
}
