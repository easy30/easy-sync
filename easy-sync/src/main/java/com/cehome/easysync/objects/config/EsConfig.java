package com.cehome.easysync.objects.config;

public class EsConfig {
    private MysqlSource mysqlSource;
    private Es es;
    private EsMapping esMapping;
    private Rebuild rebuild;
    private Repair repair;

    public MysqlSource getMysqlSource() {
        return mysqlSource;
    }

    public void setMysqlSource(MysqlSource mysqlSource) {
        this.mysqlSource = mysqlSource;
    }

    public Es getEs() {
        return es;
    }

    public void setEs(Es es) {
        this.es = es;
    }

    public EsMapping getEsMapping() {
        return esMapping;
    }

    public void setEsMapping(EsMapping esMapping) {
        this.esMapping = esMapping;
    }

    public Rebuild getRebuild() {
        return rebuild;
    }

    public void setRebuild(Rebuild rebuild) {
        this.rebuild = rebuild;
    }

    public Repair getRepair() {
        return repair;
    }

    public void setRepair(Repair repair) {
        this.repair = repair;
    }
}
