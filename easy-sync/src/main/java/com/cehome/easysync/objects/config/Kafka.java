package com.cehome.easysync.objects.config;

public class Kafka {
    private String version;
    private String servers;
    private String acks;
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
    private String fetchMessageMaxBytes;

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

    public String getAcks() {
        return acks;
    }

    public void setAcks(String acks) {
        this.acks = acks;
    }

    public String getRetries() {
        return retries;
    }

    public void setRetries(String retries) {
        this.retries = retries;
    }

    public String getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(String batchSize) {
        this.batchSize = batchSize;
    }

    public String getLingerMs() {
        return lingerMs;
    }

    public void setLingerMs(String lingerMs) {
        this.lingerMs = lingerMs;
    }

    public String getBufferMemory() {
        return bufferMemory;
    }

    public void setBufferMemory(String bufferMemory) {
        this.bufferMemory = bufferMemory;
    }

    public String getKeySerializer() {
        return keySerializer;
    }

    public void setKeySerializer(String keySerializer) {
        this.keySerializer = keySerializer;
    }

    public String getValueSerializer() {
        return valueSerializer;
    }

    public void setValueSerializer(String valueSerializer) {
        this.valueSerializer = valueSerializer;
    }

    public String getMessageMaxBytes() {
        return messageMaxBytes;
    }

    public void setMessageMaxBytes(String messageMaxBytes) {
        this.messageMaxBytes = messageMaxBytes;
    }

    public String getReplicaFetchMaxBytes() {
        return replicaFetchMaxBytes;
    }

    public void setReplicaFetchMaxBytes(String replicaFetchMaxBytes) {
        this.replicaFetchMaxBytes = replicaFetchMaxBytes;
    }

    public String getEnableAutoCommit() {
        return enableAutoCommit;
    }

    public void setEnableAutoCommit(String enableAutoCommit) {
        this.enableAutoCommit = enableAutoCommit;
    }

    public String getKeyDeserializer() {
        return keyDeserializer;
    }

    public void setKeyDeserializer(String keyDeserializer) {
        this.keyDeserializer = keyDeserializer;
    }

    public String getValueDeserializer() {
        return valueDeserializer;
    }

    public void setValueDeserializer(String valueDeserializer) {
        this.valueDeserializer = valueDeserializer;
    }

    public String getFetchMessageMaxBytes() {
        return fetchMessageMaxBytes;
    }

    public void setFetchMessageMaxBytes(String fetchMessageMaxBytes) {
        this.fetchMessageMaxBytes = fetchMessageMaxBytes;
    }
}
