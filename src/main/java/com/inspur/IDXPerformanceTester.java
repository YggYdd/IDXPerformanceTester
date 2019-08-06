package com.inspur;

import com.inspur.bean.NiFiUserHK;
import com.inspur.bean.OrganHK;
import com.inspur.jpa.NiFiUserHKJpa;
import com.inspur.jpa.OrganHKJpa;
import com.inspur.service.*;
import com.inspur.util.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class IDXPerformanceTester implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(IDXPerformanceTester.class);

    @Autowired
    NiFiUserHKJpa userJpa;

    @Autowired
    OrganHKJpa organJpa;


    private String templateId = "";
    private String baseGroupId = "";

    @Autowired
    private ProcessGroupService groupService;
    @Autowired
    private ServiceControllerService serviceService;
    @Autowired
    private FlowService flowService;
    @Autowired
    private ConnectionsService connService;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        new PorpertiesLoader().loadProperties("");
        createGroupAndRun();
//        stopAndDeleteGroup();
        System.exit(0);
    }

    public void createGroupAndRun() throws InterruptedException {
        baseGroupId = getBaseGroupId();
        LOGGER.info("Base Group Id is " + baseGroupId);
        templateId = getTemplateId();
        LOGGER.info("Template ID is " + templateId);
        List<String> groupIds = createProcessGroups();
//        groupIds = getProcessGroupIdsFromNiFi();
        doCreateAndRun(groupIds);
    }

    private void doCreateAndRun(List<String> groupIds) {
        groupIds.forEach(id -> {
            boolean isInstanceSuccess = instanceGroupTemplate(id);
            if (!isInstanceSuccess) {
                return;
            }
            Map<String, String> serviceIds = getProcessorServiceIds(id);
            if (serviceIds.size() > 0) {
                updateGroupProcessorServiceAttrs(serviceIds);
                LOGGER.info("All service attrs have been update.");
                updateGroupProcessorServiceStatus(serviceIds, ServiceStatus.ENABLED);
            }
            updateGroupStatus(id, ProcessGroupStatus.RUNNING);
        });
    }


    public void stopAndDeleteGroup(){
        baseGroupId = getBaseGroupId();
        LOGGER.info("Base Group Id is " + baseGroupId);
        List<String> groupIds = getProcessGroupIdsFromDB();
        LOGGER.info("Group number is " + groupIds.size());
        doStopAndDeleteGroup(groupIds);
    }

    private void doStopAndDeleteGroup(List<String> groupIds) {
        groupIds.forEach(id -> {
            updateGroupStatus(id, ProcessGroupStatus.STOPPED);
            Map<String, String> serviceIds = getProcessorServiceIds(id);
            if (serviceIds.size() > 0) {
                updateGroupProcessorServiceStatus(serviceIds, ServiceStatus.DISABLED);
            }
            emptyProcessorQueues(id);
            deleteGroup(id);
        });

    }

    private void emptyProcessorQueues(String groupId) {
        List<String> connIds = groupService.getNotEmptyConnections(groupId);
        connService.emptyQueue(connIds);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void deleteGroup(String id){
        String deleteId = groupService.deleteProcessGroup(id);
        if (!deleteId.equals(id)) {
            LOGGER.error("Process group " + id + " failed to del.");
        } else {
            organJpa.deleteById(id);
        }
    }

    private List<String> getProcessGroupIdsFromNiFi() {
        return flowService.getProcessGroupIds(baseGroupId);
    }

    private List<String> getProcessGroupIdsFromDB() {
        String userId = EnvUtils.getAuthUser() + "-" + EnvUtils.getREALM();
        return organJpa.findAllIdByUserId(userId);
    }


    private boolean updateGroupStatus(String groupId, ProcessGroupStatus status) {
        boolean isSuccess = false;
        String result = flowService.updateGroupStatus(groupId, status);
        for (int i = 0; i < 3; i++) {

            if ("".equals(result)) {
                LOGGER.error("Group " + groupId + " failed to update status " + status);
            } else {
                isSuccess = true;
                break;
            }
            try {
                Thread.sleep((long) (Math.pow(2, i) * 1000L));
            } catch (InterruptedException e) {
                LOGGER.error("Error to retry update group status, group id is " + groupId, e);
            }
        }
        return isSuccess;
    }


    private void updateGroupProcessorServiceAttrs(Map<String, String> ids) {
        ids.forEach((id, displayName) -> {
            if (Constants.getServiceToBeUpdated().keySet().contains(displayName)) {
                serviceService.updateServiceAttributes(id, (Map<String, String>) Constants.getServiceToBeUpdated().get(displayName));
            }
        });
    }


    private void updateGroupProcessorServiceStatus(Map<String, String> serviceIds, ServiceStatus status) {
        serviceIds.keySet().forEach(id -> serviceService.updateServiceStatus(id, status));
        LOGGER.info("All processor service has been update " + status);
    }

    private Map<String, String> getProcessorServiceIds(String groupId) {
        Map<String, String> ids = new HashMap<>();
        String result = groupService.getProcessGroupProcessors(groupId);
        JSONObject resultJson = JSONObject.fromObject(result);
        JSONArray processorsJson = resultJson.getJSONArray("processors");

        for (int i = 0; i < processorsJson.size(); i++) {
            JSONObject processorJson = processorsJson.getJSONObject(i);
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
                            ids.put(propServiceId, serviceDisplayName);
                        }
                    }
                }
            }
        }
        return ids;
    }

    private String getBaseGroupId() {
        if (!"".equals(Constants.getBaseGroupId())) {
            return Constants.getBaseGroupId();
        }
        String userId = EnvUtils.getAuthUser() + "-" + EnvUtils.getREALM();
        NiFiUserHK user = userJpa.findByUserId(userId);
        if (null == user) {
            throw new RuntimeException("Can not find " + userId + " from db. please check!");
        }
        LOGGER.info("Base group ID is " + user.getId());
        return user.getId();
    }

    private boolean instanceGroupTemplate(String groupId) {
        boolean isSuccess = false;
        for (int i = 0; i < 3; i++) {
            String result = groupService.instanceTemplate(groupId, templateId);
            if ("".equals(result)) {
                LOGGER.error("Process Group " + groupId + " instance template " + templateId + " failed.");
            } else {
                isSuccess = true;
                break;
            }
            try {
                Thread.sleep((long) (Math.pow(2, i) * 1000L));
            } catch (InterruptedException e) {
                LOGGER.error("Error to retry instance group template, group id is " + groupId, e);
            }
        }
        return isSuccess;
    }

    private List<String> createProcessGroups() {
        Map<String, String> idAndName = new HashMap<>();
        int failCount = 0;
        int successCount = 0;
        for (int i = 1; i < Constants.getGroupNum() + 1; i++) {

            String groupName = "IDXPerformance-8-" + i;
            String resultGroupId = groupService.addProcessGroup(groupName, baseGroupId);
            if (!"".equals(resultGroupId)) {
                idAndName.put(resultGroupId, groupName);
                successCount++;
            } else {
                failCount++;
                LOGGER.error("fail to create process group " + groupName);
            }
        }
        List<String> ids = saveProcessGroups2DB(idAndName);
        LOGGER.info(successCount + " groups are created successfully. " + failCount + " groups are failed!");
        return ids;
    }

    private List<String> saveProcessGroups2DB(Map<String, String> ids) {
        String userId = EnvUtils.getAuthUser() + "-" + EnvUtils.getREALM();
        List<String> savedIds = new LinkedList<>();
        ids.forEach((k, v) -> {
            OrganHK organ = new OrganHK();
            organ.setId(k);
            organ.setName(v);
            organ.setParentId("0");
            organ.setCreateTime(TimeUtils.getCurrentDateTime());
            organ.setUserId(userId);
            organJpa.save(organ);
            savedIds.add(k);
        });
        return savedIds;
    }

    private String getTemplateId() {
        if ("".equals(Constants.getTemplateId())) {
            if ("".equals(Constants.getTemplatePath())) {
                throw new RuntimeException("Can not find template id or file path. one of them should be configured. ");
            }
            return groupService.uploadTemplate(Constants.getTemplatePath());
        }
        return Constants.getTemplateId();
    }
}
