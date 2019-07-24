package com.inspur.util;

public enum ProcessGroupStatus {
    RUNNING("RUNNING"),
    STOPPED("STOPPED");


    private String status;

    ProcessGroupStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}
