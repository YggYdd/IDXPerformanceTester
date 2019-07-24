package com.inspur.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ProcessorServiceTest {

    private ProcessorService processorService;

    @Before
    public void initProcessorService() {
        processorService = new ProcessorService();
    }

    @Test
    public void should_return_processor_reversion_successfully_when_get() {
        String processorId = "46c1b743-6c1c-3e71-9fab-eac99c2dc9c7";
        Map<String, String> revision = processorService.getProcessorsRevision(processorId);
        System.out.println(revision);
        Assert.assertFalse(revision.isEmpty());
    }

    @Test
    public void should_update_processor_successfully_when_do_update() {
        String processorId = "46c1b743-6c1c-3e71-9fab-eac99c2dc9c7";
        Map<String, String> props = new HashMap<>();
        props.put("Database Connection Pooling Service", "0830dec8-016c-1000-ffff-ffffdc3e2d6f");
        String result = processorService.updateProcessorProperties(processorId, props);
        System.out.println(result);
    }

}