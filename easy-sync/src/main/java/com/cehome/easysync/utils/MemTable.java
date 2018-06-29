package com.cehome.easysync.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemTable {
    List<CSVRecord> records;
    Map<String,Map<String,CSVRecord>> map=new HashMap<>();
    public MemTable(){

    }
    public MemTable(String resource,String charset) throws  Exception {
        InputStreamReader r=new InputStreamReader( MemTable.class.getResourceAsStream(resource),charset);
        CSVParser parser= CSVFormat.DEFAULT.parse(r);
        records=parser.getRecords();



    }

    public void setIndex(String[] names){
        for(String name :names){
            Map<String,CSVRecord> m=new HashMap<>();
            for(CSVRecord record :records){
                m.put(record.get(name),record);
            }
            map.put(name,m);
        }

    }

    public CSVRecord find(String name ,String value){
        Map<String,CSVRecord> m=map.get(name);
        if(m!=null){
            return  m.get(value);
        }
        for(CSVRecord record :records){
            if(record.get(name).equals(value)){
                return record;
            }

        }
        return null;
    }
}
