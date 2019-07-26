package com.inspur.service;

import com.inspur.util.EnvUtils;
import com.inspur.util.HttpUtils;
import com.inspur.util.ProcessGroupStatus;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class FlowService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowService.class);

    private final String flowBaseUri = "/nifi-api/flow/";


    public String updateGroupStatus(String id, ProcessGroupStatus status) {
        String url = EnvUtils.getNifiUrlPrefix() + flowBaseUri + "process-groups/" + id;
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("state", status.getStatus());
        params.put("disconnectedNodeAcknowledged", false);

        String result = "";

        try {
            result = HttpUtils.doJsonPut(url, params);
        } catch (Exception e) {
            LOGGER.error("Error to update group status " + id, e);
        }
        return result;
    }

    public List<String> getProcessGroupIds(String parentGroupId) {
        List<String> ids = new LinkedList<>();
        String url = EnvUtils.getNifiUrlPrefix() + flowBaseUri + "process-groups/" + parentGroupId;
        String result = "";
        try {
            result = HttpUtils.doGet(url, null);
        } catch (Exception e) {
            LOGGER.error("Error to get process group ids " + parentGroupId, e);
        }

        ids = analysisProcessGroupIds(result);
        return ids;
    }

    private List<String> analysisProcessGroupIds(String result) {
        List<String> ids = new LinkedList<>();
        try {
            JSONObject jsonObject = JSONObject.fromObject(result);
            JSONArray processGroupsJson = jsonObject
                    .getJSONObject("processGroupFlow")
                    .getJSONObject("flow")
                    .getJSONArray("processGroups");
            processGroupsJson.stream().forEach(e -> ids.add(((JSONObject) e).getString("id")));
        } catch (Exception e) {
            LOGGER.error("Error to get group ids :" + result, e);
        }
        return ids;
    }
}
