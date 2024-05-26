package com.example.demo.service;

import com.example.demo.configuration.DataConfig;
import com.google.gson.Gson;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

@Service
public class ApiService {

    public String call(String serviceId, HashMap<String, String> requestMap) {

        String responseBody = "";

        DataConfig dataConfig = new DataConfig();

        String method = dataConfig.propertiesConfig(serviceId+".method");
        serviceId = dataConfig.propertiesConfig(serviceId+".serviceId");

        CloseableHttpClient httpClient = buildRequest();

        Gson gson = new Gson();
        String jsonBody = gson.toJson(requestMap);

        StringEntity requestEntity = new StringEntity(jsonBody, ContentType.APPLICATION_JSON);

        CloseableHttpResponse response = null;

        try {
            if("GET".equals(method)){
                HttpGet httpGet = new HttpGet(serviceId+"?"+buildQueryString(requestMap));
                response = httpClient.execute(httpGet);
            }else if("POST".equals(method)){
                HttpPost httpPost = new HttpPost(serviceId);
                httpPost.setEntity(requestEntity);
                response = httpClient.execute(httpPost);
            }

            responseBody = EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return responseBody;
    }

    public CloseableHttpClient buildRequest(){

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setSocketTimeout(5000)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        return httpClient;
    }

    public static String buildQueryString(HashMap<String, String> paramMap) {
        StringBuilder queryString = new StringBuilder();
        for (HashMap.Entry<String, String> entry : paramMap.entrySet()) {
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            try {
                queryString.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return queryString.toString();
    }
}
