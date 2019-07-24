package com.inspur.util;

import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class HttpUtilsTest {

    @Test
    public void should_return_info_success_when_do_get() throws Exception {
        String url = "http://10.111.24.82/manage-dataflow/com.inspur.service/nifi/tasktree/addNode";
        Map<String, String> params = new HashMap<>();
        params.put("id", "TTTTEST");
        params.put("parentId", "0");
        String result = HttpUtils.doFormPost(url, params);
        System.out.println(result);
    }

    @Test
    public void should_return_process_group_info_when_get() throws Exception {
        String url = "https://10.111.24.82:9091/nifi-api/process-groups/031b8506-016c-1000-0000-00001f539910";
        String result = HttpUtils.doGet(url, new HashMap<>());
        System.out.println(result);
    }

    @Test
    public void should_delete_node_success_when_delete() throws Exception {
        String url = "https://10.111.24.82:9091/nifi-api/process-groups/f8612123-016b-1000-0000-00005048539b";
        Map<String, String> params = new HashMap<>();
        params.put("version", "2");
        String result = HttpUtils.doDelete(url, params);
        System.out.println(result);
    }

    @Test
    public void auth_test() throws Exception {
        String url = "http://10.111.24.82/dev/?cluster=cluster1";
        String result = HttpUtils.doGet(url, new HashMap<>());
        System.out.println(result);

    }

    @Test
    public void test(){
        Map<String, String> test = new HashMap<>();
        test.put("111","111");
        test.put("222","222");

        System.out.println(JSONObject.fromObject(test).toString());
    }

}