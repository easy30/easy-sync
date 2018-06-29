package com.cehome.easysync.service;

import com.cehome.easysync.dao.PositionDao;
import com.cehome.easysync.domain.Position;
import jsharp.sql.JSException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PositionService {

    @Autowired
    private PositionDao positionDao;

    public Position getPosition(long timeTaskId,long serverId){
        List<Position> list= positionDao.queryListByProps(null, "timeTaskId",timeTaskId);
        if(list.size()==0) return null;
        for(Position position :list){
            if(position.getServerId()==serverId) return  position;
        }
        throw new JSException("can not find serverId "+serverId);
    }

    public int save(Position position){
      return  positionDao.save(position);
    }
}
