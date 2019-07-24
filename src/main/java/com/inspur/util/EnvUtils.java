package com.inspur.util;

public class EnvUtils {

    private static final String NIFI_URL_PREFIX = "https://10.111.24.82:9091";
    private static final String NIFI_USERNAME = "nifiadmin";
    private static final String NIFI_PASSWORD = "175d6b94-8dd9-40e9-ac7b-5df7dd55e8d9";


    private static final String AUTH_HOST = "10.111.24.82";
    private static final String AUTH_USER = "sysadmin";
    private static final String AUTH_PWD = "sysadmin";

    private static final String REALM = "realm";
    private static final String CLUSTER = "cluster1";

    public static String getNifiUrlPrefix() {
        return NIFI_URL_PREFIX;
    }

    public static String getNifiUsername() {
        return NIFI_USERNAME;
    }

    public static String getNifiPassword() {
        return NIFI_PASSWORD;
    }

    public static String getAuthHost() {
        return AUTH_HOST;
    }

    public static String getAuthUser() {
        return AUTH_USER;
    }

    public static String getAuthPwd() {
        return AUTH_PWD;
    }

    public static String getREALM() {
        return REALM;
    }

    public static String getCLUSTER() {
        return CLUSTER;
    }


}
