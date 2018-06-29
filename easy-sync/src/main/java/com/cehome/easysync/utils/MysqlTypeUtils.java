package com.cehome.easysync.utils;

import java.util.HashSet;
import java.util.Set;

public class MysqlTypeUtils {
    private static Set<String> stringTypeSet = new HashSet<String>() {
        {
            add("char");
            add("varchar");
            add("nchar");
            add("nvarchar");
            add("char");
            add("tinytext");
            add("mediumtext");
            add("longtext");
            add("text");
        }  };

    private static Set<String> dateTypeSet = new HashSet<String>() {
        {
            add("date");
            add("datetime");
            add("time");
            add("timestamp");
             //year??
        }  };

    private static Set<String> dateTypeSet2 = new HashSet<String>() {
        {
            add("date");
            add("datetime");
            add("time");
        }  };

    public static boolean isString(String dataType) {
        return stringTypeSet.contains(dataType);
    }
    public static boolean isDate(String dataType) {
        return dateTypeSet.contains(dataType);
    }
    public static boolean isDateNormal(String dataType) {
        return dateTypeSet2.contains(dataType);
    }
}
