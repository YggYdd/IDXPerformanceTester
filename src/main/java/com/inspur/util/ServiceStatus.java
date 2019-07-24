package com.inspur.util;

public enum ServiceStatus {
    ENABLED("ENABLED"),
    DISABLED("DISABLED");

    private String status;

    public String getStatus() {
        return this.status;
    }

    ServiceStatus(String status) {
        this.status = status;
    }
}
