package com.inspur.util;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    public static final String TEMPLATE_ID = "844b227f-cbd8-37ce-b9be-5415a3a49476";
    public static final String TEMPLATE_PATH = "D:\\IdeaWorkspace\\IDXPerformanceTester\\src\\main\\resources\\NiFiPerformanceTestStandardTemplate.xml";
    public static final String BASE_GROUP_ID = "f85911d8-016b-1000-0000-000037027242";
    private static final int GROUP_NUM = 1;
    private static final Map<String, Object> serviceToBeUpdated = new HashMap<>();
    static {
        Map<String, String> properties = new HashMap<>();
        properties.put("Password","123456789ijnuhb!QAZ@WSX");
        serviceToBeUpdated.put("Host-24.83", properties);
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
