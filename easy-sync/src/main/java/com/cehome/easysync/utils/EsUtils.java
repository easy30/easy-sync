package com.cehome.easysync.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class EsUtils {
    private static final Logger logger = LoggerFactory.getLogger(EsUtils.class);







    private static Map<String,String> mapping1=new HashMap<String,String>(){{
        put("bit","boolean");//todo:bit(1)  , bit(2)??
        put("boolean","boolean");

        put("date","date");
        put("datetime","date");
        put("time","date");
        put("timestamp","date");


        put("decimal","double");
        put("double","double");
        put("real","double");
        put("float","float");
        put("bigint","long");
        put("int","integer");
        put("mediumint","integer");
        put("smallint","short");
        put("tinyint","byte");


        put("char","string");
        put("nchar","string");
        put("nvarchar","string");
        put("varchar","string");

        put("tinytext","string");
        put("mediumtext","string");
        put("longtext","string");
        put("text","string");

        put("binary","binary");
        put("varbinary","binary");
        put("blob","binary");
        put("tinyblob","binary");
        put("mediumblob","binary");
        put("longblob","binary");


    }};


    private static Map<String,String> mapping2=new HashMap<String,String>(mapping1){{
        put("char","keyword");
        put("nchar","keyword");
        put("nvarchar","keyword");
        put("varchar","keyword");

        put("tinytext","text");
        put("mediumtext","text");
        put("longtext","text");
        put("text","text");
    }};


    /**
     * https://www.cnblogs.com/waterystone/p/6226356.html
     * @param mysqlType
     * @return
     */
    public static String mysqlType2EsType(String mysqlType,boolean newVersion){

        return  newVersion?mapping2.get(mysqlType):mapping1.get(mysqlType);
    }

    public static void main(String[] args) {
    }
}
