/*
 * Copyright (C) 2020 Baidu, Inc. All Rights Reserved.
 */
package utils;

import com.alibaba.fastjson.JSON;
import entity.request.Request;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import processor.ChatGPTService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author guohaohao
 */
@Slf4j
public class RequestHelper {

    /**
     * 发送get请求
     * @param url url
     * @return 请求结果
     */
    public static String httpGet(String url) {
        return httpGet(url, null);
    }

    /**
     * 发送get请求，并允许附带一个header
     * @param url url
     * @return 请求结果
     */
    public static String httpGet(String url, HashMap<String, String> headers) {

        if (StringUtils.isBlank(url)) {
            log.error("httpGet URL is blank!");
            return null;
        }

        try {
            CloseableHttpClient client = null;
            CloseableHttpResponse response = null;
            try {

                HttpGet httpGet = new HttpGet(url);
                if (headers != null) {
                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        httpGet.setHeader(entry.getKey(), entry.getValue());
                    }
                }
                client = HttpClients.createDefault();
                response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                log.info("httpGet response: {}", result);
                return result;

            } catch (Exception e) {
                log.error("httpGet Exception: {}", e.fillInStackTrace().toString());
            } finally {
                if (response != null) {
                    response.close();
                }
                if (client != null) {
                    client.close();
                }
            }
        } catch (Exception e) {
            log.error("httpGet Exception: {}", e.fillInStackTrace().toString());
        }

        return null;

    }

    /**
     * 发送post请求，并默认提供一个Content-Type: application/json的header
     * @param url url
     * @param request 请求
     * @return 请求结果
     */
    public static String httpPost(String url, Request request) {
        return httpPost(url, request, null);
    }

    /**
     * 发送post请求
     * @param url url
     * @param request 请求
     * @param headerMap 请求头
     * @return 请求结果
     */
    public static String httpPost(String url, Request request, Map<String, String> headerMap) {

        if (StringUtils.isBlank(url)) {
            log.error("httpPost URL is blank!");
            return null;
        }
        if (request == null) {
            log.error("httpPost request is null!");
            return null;
        }

        log.info("httpPost URL: {}", url);
        log.info("httpPost request: {}", JSON.toJSONString(request));
        log.info("httpPost headers: {}", JSON.toJSONString(headerMap));

        try {
            CloseableHttpClient client = null;
            CloseableHttpResponse response = null;
            try {

                HttpPost httpPost = new HttpPost(url);
                httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");
                if (headerMap != null) {
                    for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                        httpPost.addHeader(entry.getKey(), entry.getValue());
                    }
                }
                httpPost.setEntity(new StringEntity(JSON.toJSONString(request),
                        ContentType.create("text/json", "UTF-8")));

                HttpHost proxy = new HttpHost("127.0.0.1", 7890);
                if (url.equals(ChatGPTService.OPENAI_URL)) {
                    RequestConfig defaultRequestConfig = RequestConfig.custom()
                            .setConnectTimeout(5000).setSocketTimeout(20000).setProxy(proxy).build();
                    client = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build();
                } else {
                    client = HttpClients.createDefault();
                }
                response = client.execute(httpPost);
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                log.info("httpPost response: {}", result);
                return result;

            } catch (Exception e) {
                log.error("httpPost Exception: {}", e.fillInStackTrace().toString());
            } finally {
                if (response != null) {
                    response.close();
                }
                if (client != null) {
                    client.close();
                }
            }
        } catch (Exception e) {
            log.error("httpPost Exception: {}", e.fillInStackTrace().toString());
        }
        return null;
    }

}
