package com.inspur;

import com.inspur.service.ProcessGroupService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;

public class IDXPerformanceTesterTest {


    private ProcessGroupService groupService;

    @Before
    public void initService() {
        groupService = new ProcessGroupService();
    }

    @Test
    public void test() {
        Map<String, String> ids = new HashMap<>();
        String id = "21efd518-016c-1000-ffff-ffffa7987273";
        String result = groupService.getProcessGroupProcessors(id);
        JSONObject resultJson = JSONObject.fromObject(result);
        JSONArray processorsJson = resultJson.getJSONArray("processors");

        for (int i = 0; i < processorsJson.size(); i++) {
            JSONObject processorJson = processorsJson.getJSONObject(i);
            System.out.println(processorJson.toString());
            JSONObject configJson = processorJson.getJSONObject("component").getJSONObject("config");
            JSONObject propertiesJson = configJson.getJSONObject("properties");
            Iterator iterator = propertiesJson.keys();
            while (iterator.hasNext()) {
                String propName = (String) iterator.next();
                if (propName.toLowerCase().contains("service")) {
                    String propServiceId = propertiesJson.getString(propName);
                    JSONObject serviceJson = configJson.getJSONObject("descriptors").getJSONObject(propName);
                    JSONArray absJson = serviceJson.getJSONArray("allowableValues");
                    for (int j = 0; j < absJson.size(); j++) {
                        JSONObject abJson = absJson.getJSONObject(j).getJSONObject("allowableValue");
                        String serviceDisplayName = abJson.getString("displayName");
                        String serviceDisplayId = abJson.getString("value");
                        if (serviceDisplayId.equals(propServiceId)) {
                            System.out.println( serviceDisplayId + " " + serviceDisplayName);
                            ids.put(propServiceId, serviceDisplayName);
                        }
                    }
                }
            }
        }

        System.out.println(ids);
    }

}