package com.cehome.easysync.objects;

import java.util.HashMap;
import java.util.Map;

public class MysqlUrl {
    private String host;
    private String port;

    private String url;
    private long connectTimeout=5000;
    private boolean useSSL;

    // jdbc:mysql://127.0.0.1:3306?zeroDateTimeBehavior=convertToNull&connectTimeout=5000
    public MysqlUrl(String url){
        try {
            this.url = url;
            int n1 = url.indexOf("://");
            int n2 = url.indexOf("?");

            if (n2 == -1) {
                n2 = url.length();
            } else {
                String p = url.substring(n2 + 1);
                String[] params = p.split("&");
                Map<String, String> map = new HashMap<>();
                for (String param : params) {
                    String[] ss = param.split("=");
                    map.put(ss[0], ss[1]);
                }

                String value = map.get("connectTimeout");
                if (value != null) {
                    setConnectTimeout(Long.parseLong(value));
                }

                value = map.get("useSSL");
                if (value != null) {
                    setUseSSL(Boolean.parseBoolean(value));
                }


            }
            String h = url.substring(n1 + 3, n2);
            n1 = h.indexOf(':');
            setHost(h.substring(0, n1));
            setPort(h.substring(n1 + 1));
        }catch (Exception e){
            throw new RuntimeException(url+" is not valid mysql url.",e);
        }

    }


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUrl() {
        return url;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public boolean isUseSSL() {
        return useSSL;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }
}
