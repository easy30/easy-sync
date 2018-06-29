package com.cehome.easysync.objects.config;

public class Rebuild {
    private boolean enableFullSync;
    private String fullWhere;
    private int fullBatchSize;
    private int fullBatchInterval;
    private boolean switchAfterFullSync;
    private boolean deleteOldIndex;

    public boolean isEnableFullSync() {
        return enableFullSync;
    }

    public void setEnableFullSync(boolean enableFullSync) {
        this.enableFullSync = enableFullSync;
    }

    public String getFullWhere() {
        return fullWhere;
    }

    public void setFullWhere(String fullWhere) {
        this.fullWhere = fullWhere;
    }

    public int getFullBatchSize() {
        return fullBatchSize;
    }

    public void setFullBatchSize(int fullBatchSize) {
        this.fullBatchSize = fullBatchSize;
    }

    public int getFullBatchInterval() {
        return fullBatchInterval;
    }

    public void setFullBatchInterval(int fullBatchInterval) {
        this.fullBatchInterval = fullBatchInterval;
    }

    public boolean isSwitchAfterFullSync() {
        return switchAfterFullSync;
    }

    public void setSwitchAfterFullSync(boolean switchAfterFullSync) {
        this.switchAfterFullSync = switchAfterFullSync;
    }

    public boolean isDeleteOldIndex() {
        return deleteOldIndex;
    }

    public void setDeleteOldIndex(boolean deleteOldIndex) {
        this.deleteOldIndex = deleteOldIndex;
    }
}
