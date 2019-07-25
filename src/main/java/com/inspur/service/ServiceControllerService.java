package com.inspur.service;

import com.inspur.util.EnvUtils;
import com.inspur.util.HttpUtils;
import com.inspur.util.ServiceStatus;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ServiceControllerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceControllerService.class);

    private final String serviceBaseUri = "/nifi-api/controller-services/";

    public String getServiceInfo(String id) {
        String url = EnvUtils.getNifiUrlPrefix() + serviceBaseUri + id;
        String result = "";

        try {
            result = HttpUtils.doGet(url, null);
        } catch (Exception e) {
            LOGGER.error("Error to get service info " + id, e);
        }
        return result;
    }

    public String updateServiceStatus(String id, ServiceStatus status) {
        String url = EnvUtils.getNifiUrlPrefix() + serviceBaseUri + id + "/run-status";
        Map<String, String> revision = getServiceRevision(id);
        Map<String, Object> params = new HashMap<>();
        params.put("revision", revision);
        params.put("disconnectedNodeAcknowledged", "false");
        params.put("state", status.getStatus());

        String result = "";

        try {
            result = HttpUtils.doJsonPut(url, params);
        } catch (Exception e) {
            LOGGER.error("Error to update service status " + id, e);
        }
        return result;
    }

    public String updateServiceAttributes(String id, Map<String, String> attrs) {
        String url = EnvUtils.getNifiUrlPrefix() + serviceBaseUri + id;
        Map<String, String> revision = getServiceRevision(id);
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> component = new HashMap<>();
        component.put("id", id);
        component.put("properties", attrs);
        params.put("revision", revision);
        params.put("disconnectedNodeAcknowledged", "false");
        params.put("component", component);

        String returnId = "";
        String result = "";
        try {
            result = HttpUtils.doJsonPut(url, params);
            returnId = getIdFromResult(result);
        } catch (Exception e) {
            LOGGER.error(result);
            LOGGER.error("Error to update attributes " + id, e);
        }
        return returnId;
    }

    public Map<String, String> getServiceRevision(String id) {
        Map<String, String> clientIdAndVersion = new HashMap<>();
        String serviceInfo = getServiceInfo(id);
        if ("".equals(serviceInfo)) {
            return clientIdAndVersion;
        }
        JSONObject jsonInfo = JSONObject.fromObject(serviceInfo);
        JSONObject jsonRevision = jsonInfo.getJSONObject("revision");
        clientIdAndVersion.put("version", jsonRevision.getString("version"));
        if (jsonRevision.has("clientId")) {
            clientIdAndVersion.put("clientId", jsonRevision.getString("clientId"));
        } else {
            clientIdAndVersion.put("clientId", UUID.randomUUID().toString());
        }
        return clientIdAndVersion;
    }

    public String getServiceName(String id) {
        String serviceInfo = getServiceInfo(id);
        if ("".equals(serviceInfo)) {
            return "";
        }
        String name = "";
        try {
            name = getServiceNameFromResult(serviceInfo);
        } catch (Exception e) {
            LOGGER.error(e.toString());
        }
        return name;
    }

    private String getServiceNameFromResult(String json) {
        JSONObject resultJson = JSONObject.fromObject(json);
        return resultJson.getJSONObject("component").getString("name");
    }

    private String getIdFromResult(String json) {
        JSONObject resultJson = JSONObject.fromObject(json);
        return resultJson.getString("id");
    }
}
