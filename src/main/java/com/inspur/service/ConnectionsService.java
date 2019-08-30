package com.inspur.service;

import com.inspur.util.EnvUtils;
import com.inspur.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class ConnectionsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionsService.class);

    private final String connBaseUri = "/nifi-api/flowfile-queues/";

    public ConnectionsService() {
    }

    public void emptyQueue(List<String> ids) {
        ids.forEach(id -> emptyQueue(id));
    }

    public void emptyQueue(String id) {
        String url = EnvUtils.getNifiUrlPrefix() + connBaseUri + id + "/drop-requests";

        String result = "";

        try {
            result = HttpUtils.doJsonPost(url, new HashMap<>());
        } catch (Exception e) {
            LOGGER.error("Error to empty queue " + id, e);
        }
    }
}
