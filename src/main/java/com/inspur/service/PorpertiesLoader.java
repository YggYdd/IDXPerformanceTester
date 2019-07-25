package com.inspur.service;

import com.inspur.bean.ConstantsProperties;
import com.inspur.bean.EnvProperties;
import com.inspur.util.Constants;
import com.inspur.util.EnvUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PorpertiesLoader {

    private String defaultPath = "conf/IDXApplication.properties";

    public void loadProperties(String path) throws IOException {
        InputStream inputStream;
        if ("".equals(path)) {
            inputStream = ClassLoader.getSystemResourceAsStream(defaultPath);
        } else {
            inputStream = ClassLoader.getSystemResourceAsStream(path);
        }
        Properties prop = new Properties();
        prop.load(inputStream);
        initEnvProperties(prop);
        initConstantProperties(prop);
    }

    private void initEnvProperties(Properties prop) {
        EnvProperties envProperties = new EnvProperties();
        envProperties.setAuthHost(prop.getProperty("authHost"));
        envProperties.setAuthUser(prop.getProperty("authUser"));
        envProperties.setAuthPasswd(prop.getProperty("authPasswd"));
        envProperties.setRealm(prop.getProperty("realm"));
        envProperties.setCluster(prop.getProperty("cluster"));
        EnvUtils.setEnvProperties(envProperties);
    }

    private void initConstantProperties(Properties prop) {
        ConstantsProperties constantsProperties = new ConstantsProperties();
        constantsProperties.setBaseGroupId(prop.getProperty("baseGroupId"));
        constantsProperties.setTemplateId(prop.getProperty("templateId"));
        constantsProperties.setTemplatePath(prop.getProperty("templatePath"));
        constantsProperties.setGroupNum(Integer.parseInt(prop.getProperty("groupNum")));
        Constants.setConstantsProperties(constantsProperties);
    }

  /*  public static void main(String[] args) throws IOException {
        PorpertiesLoader loader = new PorpertiesLoader();
        loader.loadProperties("");
    }*/
}
