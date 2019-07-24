package com.inspur.service;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class ServiceControllerServiceTest {

    ServiceControllerService service;

    @Before
    public void initService() {
        service = new ServiceControllerService();
    }

    @Test
    public void should_return_service_revision_when_get() {
        String id = "d076df6d-889c-3468-b8b0-4da75149772d";
        Map<String, String> revision = service.getServiceRevision(id);
        System.out.println(revision);
    }

    @Test
    public void should_return_service_info_when_get(){
        String id = "30829cd5-22e0-3381-bdca-06be8a0e3981";
        String info =service.getServiceInfo(id);
        System.out.println(info);
    }

}