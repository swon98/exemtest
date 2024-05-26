package com.example.demo.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DataConfig {

    public String propertiesConfig(String name){

        Properties properties = new Properties();
        String propertiesFilePath = "properties/api.properties";
        String apiUrl = "";
        try (InputStream input = DataConfig.class.getClassLoader().getResourceAsStream(propertiesFilePath)) {
            properties.load(input);
            apiUrl = properties.getProperty(name);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return apiUrl;
    }

}
