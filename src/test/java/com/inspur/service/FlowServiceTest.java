package com.inspur.service;

import com.inspur.util.ProcessGroupStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class FlowServiceTest {
    FlowService flowService;

    @Before
    public void initFlowServie() {
        flowService = new FlowService();
    }


    @Test
    public void should_process_group_status_update_when_do() {
        String id = "185cc0ca-016c-1000-0000-000039f6d387";
        String result = flowService.updateGroupStatus(id, ProcessGroupStatus.STOPPED);
        System.out.println(result);
        Assert.assertTrue(result.contains(ProcessGroupStatus.STOPPED.getStatus()));
    }

    @Test
    public void should_return_parent_group_children_group_ids_when_get(){
        String parentId = "f85911d8-016b-1000-0000-000037027242";
        List<String> childrenIds = flowService.getProcessGroupIds(parentId);
        System.out.println(childrenIds);
    }

}