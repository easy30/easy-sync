package com.cehome.easysync.objects.config;

import java.io.Serializable;

public class Mysql implements Serializable {

    //defalut =  jdbc:mysql://127.0.0.1:3306?zeroDateTimeBehavior=convertToNull&connectTimeout=5000
    private String url;
    private String user;
    private String password;
    //default
    //private String bingLogCharset;


    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
