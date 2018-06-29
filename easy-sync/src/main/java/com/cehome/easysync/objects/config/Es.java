package com.cehome.easysync.objects.config;

public class Es {
    private String addresses;
    private String analyzer;
    private String indexAlias;
    private String indexSettings;
    // 0 <5  1 >=5
    private int version;

    public String getAddresses() {
        return addresses;
    }

    public void setAddresses(String addresses) {
        this.addresses = addresses;
    }

    public String getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }

    public String getIndexAlias() {
        return indexAlias;
    }

    public void setIndexAlias(String indexAlias) {
        this.indexAlias = indexAlias;
    }

    public String getIndexSettings() {
        return indexSettings;
    }

    public void setIndexSettings(String indexSettings) {
        this.indexSettings = indexSettings;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
