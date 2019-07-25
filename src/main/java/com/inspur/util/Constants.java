package com.inspur.util;

import com.inspur.bean.ConstantsProperties;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    private static ConstantsProperties constantsProperties;

    public static final String TEMPLATE_ID = "02e2af51-e703-362b-b642-bcfc58199427";
    public static final String TEMPLATE_PATH = "D:\\IdeaWorkspace\\IDXPerformanceTester\\src\\main\\resources\\IDXPerformanceTemplateHK.xml";
    public static final String BASE_GROUP_ID = "f3748d45-016b-1000-0000-00006ebe7dff";
    private static final int GROUP_NUM = 1000;
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
        return GROUP_NUM;
    }

    public static String getTemplateId() {
        return TEMPLATE_ID;
    }

    public static String getTemplatePath() {
        return TEMPLATE_PATH;
    }

    public static String getBaseGroupId() {
        return BASE_GROUP_ID;
    }

    public static Map<String, Object> getServiceToBeUpdated() {
        return serviceToBeUpdated;
    }
}
