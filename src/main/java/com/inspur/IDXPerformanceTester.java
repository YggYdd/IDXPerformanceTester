package com.inspur;

import com.inspur.bean.NiFiUser;
import com.inspur.bean.Organ;
import com.inspur.jpa.NiFiUserJpa;
import com.inspur.jpa.OrganJpa;
import com.inspur.service.FlowService;
import com.inspur.service.ProcessGroupService;
import com.inspur.service.ServiceControllerService;
import com.inspur.util.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class IDXPerformanceTester implements ApplicationRunner {

    @Autowired
    NiFiUserJpa userJpa;

    @Autowired
    OrganJpa organJpa;

    private List<String> groupIds = new LinkedList<>();
    private String templateId = "";
    private String baseGroupId = "";

    @Autowired
    private ProcessGroupService groupService;
    @Autowired
    private ServiceControllerService serviceService;
    @Autowired
    private FlowService flowService;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        createGroupAndRun();
//        stopAndDeleteGroup();
        System.exit(0);
    }

    public void createGroupAndRun() {
        baseGroupId = getBaseGroupId();
        templateId = getTemplateId();
        System.out.println("Template ID is " + templateId);
        groupIds = createProcessGroups();
        instanceGroupTemplate();
        Map<String, String> serviceIds = getProcessorServiceIds();
        updateGroupProcessorServiceAttrs(serviceIds);
        updateGroupProcessorServiceStatus(serviceIds, ServiceStatus.ENABLED);
        updateGroupStatus(ProcessGroupStatus.RUNNING);
    }


    public void stopAndDeleteGroup() {
        baseGroupId = getBaseGroupId();
        groupIds = getProcessGroupIds();
        updateGroupStatus(ProcessGroupStatus.STOPPED);
        Map<String, String> serviceIds = getProcessorServiceIds();
        updateGroupProcessorServiceStatus(serviceIds, ServiceStatus.DISABLED);
        deleteGroups();
    }

    private void deleteGroups() {
        groupIds.forEach(id -> {
            String deleteId = groupService.deleteProcessGroup(id);
            if (!deleteId.equals(id)) {
                System.out.println("Process group " + id + " failed to del.");
            } else {
                organJpa.deleteById(id);
            }
        });
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
                System.out.println("Group " + id + " failed to update status " + status);
            } else {
                successCount++;
            }
        }
        System.out.println("Update group status " + status + " success " + successCount + " fail " + failCount);
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
    }

    private Map<String, String> getProcessorServiceIds() {
        Map<String, String> ids = new HashMap<>();
        for (String id : groupIds) {
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
                        for (int j = 0; j<absJson.size(); j++){
                            JSONObject abJson = absJson.getJSONObject(j).getJSONObject("allowableValue");
                            String serviceDisplayName = abJson.getString("displayName");
                            String serviceDisplayId = abJson.getString("value");
                            if (serviceDisplayId.equals(propServiceId)){
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
        String userId = EnvUtils.getAuthUser() + "-" + EnvUtils.getCLUSTER();
        NiFiUser user = userJpa.findByUserId(userId);
        if (null == user) {
            throw new RuntimeException("Can not find " + userId + " from db. please check!");
        }
        System.out.println("Base group ID is " + user.getId());
        return user.getId();
    }

    private void instanceGroupTemplate() {
        int failCount = 0;
        int successCount = 0;
        for (String id : groupIds) {
            String result = groupService.instanceTemplate(id, templateId);
            if ("".equals(result)) {
                failCount++;
                System.out.println("Process Group " + id + " instance template " + templateId + " failed.");
            } else {
                successCount++;
            }
        }
        System.out.println("Instance group template success count is " + successCount + ", fail count is " + failCount);
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
                System.out.println("fail to create process group " + groupName);
            }
        }
        List<String> ids = saveProcessGroups2DB(idAndName);
        System.out.println(successCount + " groups are created successfully. " + failCount + " groups are failed!");
        return ids;
    }

    private List<String> saveProcessGroups2DB(Map<String, String> ids) {
        String userId = EnvUtils.getAuthUser() + "-" + EnvUtils.getCLUSTER();
        List<String> savedIds = new LinkedList<>();
        ids.forEach((k, v) -> {
            Organ organ = new Organ();
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
        if ("".equals(Constants.TEMPLATE_ID)) {
            if ("".equals(Constants.TEMPLATE_PATH)) {
                throw new RuntimeException("Can not find template id or file path. one of them should be configured. ");
            }
            return groupService.uploadTemplate(Constants.TEMPLATE_PATH);
        }
        return Constants.TEMPLATE_ID;
    }
}
