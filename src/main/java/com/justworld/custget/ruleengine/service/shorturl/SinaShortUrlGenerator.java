package com.justworld.custget.ruleengine.service.shorturl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 新浪短链接生成器
 */
@Slf4j
@Service
public class SinaShortUrlGenerator implements IShortUrlGenerator {

    @Value("${short-url-server.sina.url}")
    private String serverUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void convertShortUrl(Map<String, String> urlMap) {

        //如果没有token，则发提醒短信(一天一次) TODO

        String token = "2.00z3GCYBdvdC_E514f2c1be10L8mHf";

        //发送请求
        StringBuilder url = new StringBuilder(serverUrl + "?access_token={1}").append(urlMap.keySet().stream().reduce("",(k1, k2) -> k1.concat("&url_long=" + k2)));
        Map result = restTemplate.getForObject(url.toString(), Map.class, token);
        for (Map<String, ?> urls : (List<Map<String, ?>>) result.get("urls")) {
            urlMap.put(urls.get("url_long") + "", urls.get("url_short") + "");
        }
        log.debug("short urls = {}", result.get("urls"));

    }
}
