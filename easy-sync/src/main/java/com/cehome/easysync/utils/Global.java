package com.cehome.easysync.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;

public class Global {
    public static JSONObject toJSON(String s){
        if(StringUtils.isBlank(s)) return new JSONObject();
        return  JSONObject.parseObject(s);
    }

    public static <T>T toObject(String s, Class<T> c)  {
        try {
            if (StringUtils.isBlank(s)) return c.newInstance();
            return JSON.parseObject(s, c);
        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }

    public static JSONObject toObject(String s)  {
        try {
            if (StringUtils.isBlank(s)) return new JSONObject();
            return JSON.parseObject(s);
        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }
}
