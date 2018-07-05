package com.cehome.easysync.objects.config;

public class Kafka {
    private String version="0.10.1.0";
    private String servers;
    private String producerConfigs;
    private String consumerConfigs;
   /* private String acks;
    private String retries;
    private String batchSize;
    private String lingerMs;
    private String bufferMemory;
    private String keySerializer;
    private String valueSerializer;
    private String messageMaxBytes;
    private String replicaFetchMaxBytes;

    private String enableAutoCommit;
    private String keyDeserializer;
    private String valueDeserializer;
    private String fetchMessageMaxBytes;*/

    public String getServers() {
        return servers;
    }

    public void setServers(String servers) {
        this.servers = servers;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProducerConfigs() {
        return producerConfigs;
    }

    public void setProducerConfigs(String producerConfigs) {
        this.producerConfigs = producerConfigs;
    }

    public String getConsumerConfigs() {
        return consumerConfigs;
    }

    public void setConsumerConfigs(String consumerConfigs) {
        this.consumerConfigs = consumerConfigs;
    }
}
