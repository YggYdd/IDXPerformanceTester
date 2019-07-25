package com.inspur.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PorpertiesLoader {

    public void loadProperties() throws IOException {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream("/demo/jdbc.properties");
        Properties prop = new Properties();
        prop.load(inputStream);
        String driverClassName = prop.getProperty("driverClassName");
    }
}
