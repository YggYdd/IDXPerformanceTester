package com.inspur.service;


import com.inspur.util.EnvUtils;
import com.inspur.util.HttpUtils;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProcessorService {

    private final String processorsBaseUri = "/nifi-api/processors/";

    public String getProcessorInfo(String id) {
        String getUrl = EnvUtils.getNifiUrlPrefix() + processorsBaseUri + id;
        String result = "";

        try {
            result = HttpUtils.doGet(getUrl, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Map<String, String> getProcessorsRevision(String id) {
        Map<String, String> clientIdAndVersion = new HashMap<>();
        String processorInfo = getProcessorInfo(id);
        if ("".equals(processorInfo)) {
            return clientIdAndVersion;
        }
        JSONObject jsonInfo = JSONObject.fromObject(processorInfo);
        JSONObject jsonRevision = jsonInfo.getJSONObject("revision");
        clientIdAndVersion.put("version", jsonRevision.getString("version"));
        if (jsonRevision.has("clientId")) {
            clientIdAndVersion.put("clientId", jsonRevision.getString("clientId"));
        } else {
            clientIdAndVersion.put("clientId", UUID.randomUUID().toString());
        }
        return clientIdAndVersion;
    }

    public String updateProcessorProperties(String id, Map<String, String> props) {
        String updateUrl = EnvUtils.getNifiUrlPrefix() + processorsBaseUri + id;
        Map<String, String> revision = getProcessorsRevision(id);

        Map<String, Object> config = new HashMap<>();
        config.put("properties", props);

        Map<String, Object> component = new HashMap<>();
        component.put("id", id);
        component.put("config", config);

        Map<String, Object> settings = new HashMap<>();
        settings.put("revision", revision);
        settings.put("component", component);
        settings.put("disconnectedNodeAcknowledged", false);

        String result = "";
        try {
            result = HttpUtils.doJsonPut(updateUrl, settings);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
