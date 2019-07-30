package com.inspur.service;

import com.inspur.util.EnvUtils;
import com.inspur.util.HttpUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Service
public class ProcessGroupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessGroupService.class);

    private final String processGroupBaseUri = "/nifi-api/process-groups/";

    public String getProcessGroupInfo(String id) {
        String getUrl = EnvUtils.getNifiUrlPrefix() + processGroupBaseUri + id;
        String result = "";
        try {
            result = HttpUtils.doGet(getUrl, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getProcessGroupProcessors(String groupId) {
        String getUrl = EnvUtils.getNifiUrlPrefix() + processGroupBaseUri + groupId + "/processors";
        String result = "";
        try {
            result = HttpUtils.doGet(getUrl, null);
        } catch (Exception e) {
            LOGGER.error("Error to get group processors ", e);
        }
        return result;
    }

    public List<String> getProcessGroupConnections(String groupId) {
        List<String> connIds = new LinkedList<>();
        String getUrl = EnvUtils.getNifiUrlPrefix() + processGroupBaseUri + groupId + "/connections";
        try {
            String result = HttpUtils.doGet(getUrl, null);
            connIds = getConnIdsFromConnInfo(result);
        } catch (Exception e) {
            LOGGER.error("Error to get group connections ", e);
        }
        return connIds;
    }

    private List<String> getConnIdsFromConnInfo(String connInfo) {
        List<String> connIds = new LinkedList<>();
        JSONObject connInfoJson = JSONObject.fromObject(connInfo);
        JSONArray connsJson = connInfoJson.getJSONArray("connections");
        Iterator<JSONObject> iterator = connsJson.iterator();
        while (iterator.hasNext()) {
            JSONObject connJson = iterator.next();
            String id = connJson.getString("id");
            connIds.add(id);
        }
        return connIds;
    }

    public List<String> getNotEmptyConnections(String groupId) {
        List<String> connIds = new LinkedList<>();
        String getUrl = EnvUtils.getNifiUrlPrefix() + processGroupBaseUri + groupId + "/connections";
        String result = "";
        try {
            result = HttpUtils.doGet(getUrl, null);
            connIds = getNotEmptyConnIdsFromConnInfo(result);
        } catch (Exception e) {
            LOGGER.error("Error to get group not empty connections", e);
            LOGGER.error("Result is :" + result);
        }
        return connIds;
    }

    private List<String> getNotEmptyConnIdsFromConnInfo(String connInfo) {
        List<String> connIds = new LinkedList<>();
        JSONObject connInfoJson = JSONObject.fromObject(connInfo);
        JSONArray connsJson = connInfoJson.getJSONArray("connections");
        Iterator<JSONObject> iterator = connsJson.iterator();
        while (iterator.hasNext()) {
            JSONObject connJson = iterator.next();
            JSONObject snapshot = connJson.getJSONObject("status").getJSONObject("aggregateSnapshot");
            int ffQueued = snapshot.getInt("flowFilesQueued");
            int queuedCount = Integer.parseInt(snapshot.getString("queuedCount").replace(",", ""));
            if (ffQueued > 0 || queuedCount > 0) {
                String id = connJson.getString("id");
                connIds.add(id);
            }
        }
        return connIds;
    }

    public String addProcessGroup(String name, String id) {
        String url = EnvUtils.getNifiUrlPrefix() + processGroupBaseUri + id + "/process-groups";

        Map<String, Object> params = new HashMap<>();
        Map<String, Integer> revision = new HashMap<>();
        Map<String, Object> component = new HashMap<>();
        Map<String, Integer> position = new HashMap<>();
        params.put("revision", revision);
        params.put("component", component);
        params.put("position", position);
        revision.put("version", 0);
        component.put("name", name);
        component.put("position", position);
        position.put("x", (int) (Math.random() * 8000));
        position.put("y", (int) (Math.random() * 6000));

        String result;
        try {
            result = HttpUtils.doJsonPost(url, params);
        } catch (Exception e) {
            LOGGER.error("Error to add group ", e);
            return "";
        }
        return getProcessGroupId(result);
    }


    public String deleteProcessGroup(String id) {
        String url = EnvUtils.getNifiUrlPrefix() + processGroupBaseUri + id;
        Map<String, String> params = getProcessGroupRevision(id);

        String result = "";
        try {
            result = HttpUtils.doDelete(url, params);
        } catch (Exception e) {
            LOGGER.error("Error to del group ", e);
        }

        if (!"".equals(result)) {
            return getProcessGroupId(result);
        }
        return result;
    }

    private Map<String, String> getProcessGroupRevision(String id) {
        Map<String, String> clientIdAndVersion = new HashMap<>();
        String processorInfo = getProcessGroupInfo(id);
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


    public String uploadTemplate(String path) {
        String uploadTemplateURL = EnvUtils.getNifiUrlPrefix() + processGroupBaseUri + "root/templates/upload";
        File templateFile = new File(path);
        Map<String, File> files = new HashMap<>();
        files.put("template", templateFile);

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/xml, text/xml, */*; q=0.01");
        headers.put("X-Requested-With", "XMLHttpRequest");
        String result = "";
        try {
            result = HttpUtils.doFilePost(uploadTemplateURL, headers, null, files);
        } catch (Exception e) {
            LOGGER.error("Error to upload template.", e);
        }
        return getTemplateId(result);
    }

    public String instanceTemplate(String groupId, String templateId) {
        String instanceTmpUrl = EnvUtils.getNifiUrlPrefix() + processGroupBaseUri + groupId + "/template-instance";
        Map<String, Object> params = new HashMap<>();
        params.put("templateId", templateId);
        params.put("originX", 250);
        params.put("originY", 250);
        params.put("disconnectedNodeAcknowledged", false);
        String result = "";
        try {
            result = HttpUtils.doJsonPost(instanceTmpUrl, params);
        } catch (Exception e) {
            LOGGER.error("Error to instance template ", e);
            LOGGER.error("Result is : " + result);
        }
        return result;
    }

    private String getProcessGroupId(String json) {
        try {
            return JSONObject.fromObject(json).getString("id");
        } catch (Exception e) {
            LOGGER.error("Error to get group id ", e);
            return "";
        }
    }

    private String getTemplateId(String xml) {
        Document doc;
        try {
            doc = DocumentHelper.parseText(xml);
        } catch (DocumentException e) {
            LOGGER.error("Error to get template id", e);
            return "";
        }
        Element rootElement = doc.getRootElement();
        Element idElement = rootElement.element("template").element("id");
        return idElement.getText();
    }

}
