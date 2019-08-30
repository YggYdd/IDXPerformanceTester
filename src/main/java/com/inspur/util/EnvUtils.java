package com.inspur.util;

import com.inspur.bean.EnvProperties;

public class EnvUtils {

    private static EnvProperties envProperties;

    public static void setEnvProperties(EnvProperties envProperties) {
        EnvUtils.envProperties = envProperties;
    }

    private static final String NIFI_USERNAME = "nifiadmin";
    private static final String NIFI_PASSWORD = "175d6b94-8dd9-40e9-ac7b-5df7dd55e8d9";

    public static String getNifiUrlPrefix() {
        return envProperties.getNifiUrlPrefix();
    }

    public static String getNifiUsername() {
        return NIFI_USERNAME;
    }

    public static String getNifiPassword() {
        return NIFI_PASSWORD;
    }

    public static String getAuthHost() {
        return envProperties.getAuthHost();
    }

    public static String getAuthUser() {
        return envProperties.getAuthUser();
    }

    public static String getAuthPwd() {
        return envProperties.getAuthPasswd();
    }

    public static String getREALM() {
        return envProperties.getRealm();
    }

    public static String getCLUSTER() {
        return envProperties.getCluster();
    }


}
