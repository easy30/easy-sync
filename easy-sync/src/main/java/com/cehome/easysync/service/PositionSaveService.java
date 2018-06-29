package com.cehome.easysync.service;

import com.cehome.easysync.domain.Position;

import java.util.Objects;

public class PositionSaveService {

    PositionService positionService;
    Position position;
    Position oldPosition=new Position();
    public PositionSaveService(PositionService positionService, Position position){
        this.positionService=positionService;
        this.position=position;
        updateOldPosition();


    }
    public void set(String filename,long posi,String gtidSet){

        position.setFilename(filename);
        position.setPosition(posi);
        position.setGtidSet(gtidSet);
    }
    public  void save(){
        //if( (timeCal.isTimeUp()>0 || now) && !same() ) {
        if(!same()){
            positionService.save(position);
            updateOldPosition();
        }
    }

    private void updateOldPosition(){
        oldPosition.setFilename(position.getFilename());
        oldPosition.setPosition(position.getPosition());
        oldPosition.setGtidSet(position.getGtidSet());
    }

    private boolean same(){
        return Objects.equals(position.getFilename(),oldPosition.getFilename()) &&
                position.getPosition()==oldPosition.getPosition() &&
                Objects.equals( position.getGtidSet(),oldPosition.getGtidSet());
    }

}
