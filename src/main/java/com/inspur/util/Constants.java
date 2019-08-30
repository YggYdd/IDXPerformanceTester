package com.inspur.util;

import com.inspur.bean.ConstantsProperties;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    private static ConstantsProperties constantsProperties;

    private static final Map<String, Object> serviceToBeUpdated = new HashMap<>();
    static {
        Map<String, String> properties = new HashMap<>();
        properties.put("Password", "1qazXSW@??");
        serviceToBeUpdated.put("Host-2.173", properties);
    }

    public static void setConstantsProperties(ConstantsProperties constantsProperties) {
        Constants.constantsProperties = constantsProperties;
    }

    public static int getGroupNum() {
        return constantsProperties.getGroupNum();
    }

    public static String getTemplateId() {
        return constantsProperties.getTemplateId();
    }

    public static String getTemplatePath() {
        return constantsProperties.getTemplatePath();
    }

    public static String getBaseGroupId() {
        return constantsProperties.getBaseGroupId();
    }

    public static Map<String, Object> getServiceToBeUpdated() {
        return serviceToBeUpdated;
    }
}
