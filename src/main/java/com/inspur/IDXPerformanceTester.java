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

    private List<String> groupIds = new LinkedList<>();
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
        groupIds = createProcessGroups();
        instanceGroupTemplate();
        Map<String, String> serviceIds = getProcessorServiceIds();
        LOGGER.info("Service number is " + serviceIds.size());
        updateGroupProcessorServiceAttrs(serviceIds);
        LOGGER.info("All service attrs have been update.");
        updateGroupProcessorServiceStatus(serviceIds, ServiceStatus.ENABLED);
//        updateGroupStatus(ProcessGroupStatus.RUNNING);
    }


    public void stopAndDeleteGroup() throws InterruptedException {
        baseGroupId = getBaseGroupId();
        LOGGER.info("Base Group Id is " + baseGroupId);
        groupIds = getProcessGroupIds();
        LOGGER.info("Group number is " + groupIds.size());
        updateGroupStatus(ProcessGroupStatus.STOPPED);
        Map<String, String> serviceIds = getProcessorServiceIds();
        LOGGER.info("Service number is " + serviceIds.size());
        updateGroupProcessorServiceStatus(serviceIds, ServiceStatus.DISABLED);
        emptyQueues();
        deleteGroups();
    }

    private void emptyQueues() {
        groupIds.forEach(id -> {
            List<String> connIds = groupService.getNotEmptyConnections(id);
            connService.emptyQueue(connIds);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        LOGGER.info("All queue have been emptied.");
    }

    private void deleteGroups() {
        groupIds.forEach(id -> {
            String deleteId = groupService.deleteProcessGroup(id);
            if (!deleteId.equals(id)) {
                LOGGER.error("Process group " + id + " failed to del.");
            } else {
                organJpa.deleteById(id);
            }
        });
        LOGGER.info("All groups have been deleted.");
    }

    private List<String> getProcessGroupIds() {
        //todo 数据库查询？？
        return flowService.getProcessGroupIds(baseGroupId);
    }

    private void updateGroupStatus(ProcessGroupStatus status) {
        int failCount = 0;
        int successCount = 0;
        for (String id : groupIds) {
            String result = flowService.updateGroupStatus(id, status);
            if ("".equals(result)) {
                failCount++;
                LOGGER.error("Group " + id + " failed to update status " + status);
            } else {
                successCount++;
            }
        }
        LOGGER.info("Update group status " + status + " success " + successCount + " fail " + failCount);
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

    private Map<String, String> getProcessorServiceIds() {
        Map<String, String> ids = new HashMap<>();
        for (String id : groupIds) {
            String result = groupService.getProcessGroupProcessors(id);
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
        }
        return ids;
    }

    private String getBaseGroupId() {
        if (!"".equals(Constants.getBaseGroupId())) {
            return Constants.getBaseGroupId();
        }
        String userId = EnvUtils.getAuthUser() + "-" + EnvUtils.getCLUSTER();
        NiFiUserHK user = userJpa.findByUserId(userId);
        if (null == user) {
            throw new RuntimeException("Can not find " + userId + " from db. please check!");
        }
        LOGGER.info("Base group ID is " + user.getId());
        return user.getId();
    }

    private void instanceGroupTemplate() throws InterruptedException {
        int failCount = 0;
        int successCount = 0;
        for (String id : groupIds) {
            String result = groupService.instanceTemplate(id, templateId);
            if ("".equals(result)) {
                failCount++;
                LOGGER.error("Process Group " + id + " instance template " + templateId + " failed.");
            } else {
                successCount++;
            }
            Thread.sleep(1000);
        }
        LOGGER.info("Instance group template success count is " + successCount + ", fail count is " + failCount);
    }

    private List<String> createProcessGroups() {
        Map<String, String> idAndName = new HashMap<>();
        int failCount = 0;
        int successCount = 0;
        for (int i = 0; i < Constants.getGroupNum(); i++) {

            String groupName = "IDXPerformance-" + i;
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
