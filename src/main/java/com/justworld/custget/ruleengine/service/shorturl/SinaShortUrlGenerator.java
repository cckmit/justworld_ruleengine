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
        //发送请求
        Map param = new HashMap();
        param.put("access_token", "2.00z3GCYBdvdC_E514f2c1be10L8mHf");
        param.put("url_long", "http://115.28.235.146:6010/fantds/");
        try {
            Map result = restTemplate.getForObject("https://api.weibo.com/2/short_url/shorten.json?access_token=2.00z3GCYBdvdC_E514f2c1be10L8mHf&url_long=http://115.28.235.146:6010/fantds/", Map.class);

        for (Map urls : (List<Map>) result.get("urls")) {
            urlMap.put(urls.get("url_long")+"", urls.get("url_short")+"");
        }
        log.debug("short urls = {}", result.get("urls"));
        } catch (Exception e){
            log.debug("生成短链接出错", e);
        }

    }
}
