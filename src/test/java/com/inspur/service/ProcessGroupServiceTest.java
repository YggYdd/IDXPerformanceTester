package com.inspur.service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;


public class ProcessGroupServiceTest {

    private ProcessGroupService processGroupService;

    @Before
    public void initProcessGroupService() {
        processGroupService = new ProcessGroupService();
    }

    @Test
    public void should_add_progress_group_success_when_do_add() throws Exception {
        String name = "AutoCreateTest";
        String processGroupId = processGroupService.addProcessGroup(name, "f85911d8-016b-1000-0000-000037027242");
        System.out.println(processGroupId);
        Assert.assertNotEquals("", processGroupId);
    }

    @Test
    public void should_upload_template_success_when_do_upload() {
        String templateFilePath = "D:\\TestForTest.xml";
        String result = processGroupService.uploadTemplate(templateFilePath);
        System.out.println(result);
    }

    @Test
    public void should_get_processor_service_when_get_from_group() {
        String id = "17ca2ed7-016c-1000-ffff-ffffe506f6e9";
        String result = processGroupService.getProcessGroupProcessors(id);
        JSONObject resultJson = JSONObject.fromObject(result);
        JSONArray processorsJson = resultJson.getJSONArray("processors");
        for (int i = 0; i < processorsJson.size(); i++) {
            JSONObject processorJson = processorsJson.getJSONObject(i);
            System.out.println(processorJson.toString());
            JSONObject configJson = processorJson.getJSONObject("component").getJSONObject("config");
            JSONObject propertiesJson = configJson.getJSONObject("properties");
            Iterator iterator = propertiesJson.keys();
        }
    }


    @Test
    public void should_return_process_revision_when_get() {
        String id = "18ac0a49-016c-1000-ffff-ffffe5eeb4b6";
        String revision = processGroupService.getProcessGroupInfo(id);
        System.out.println(revision);
    }

    @Test
    public void should_return_process_group_processors_info_successfully_when_get() {
        String groupId = "1e292c02-016c-1000-0000-000079d1cbaa";
        String result = processGroupService.getProcessGroupProcessors(groupId);
        System.out.println(result);
        Assert.assertNotEquals("", result);
    }

}