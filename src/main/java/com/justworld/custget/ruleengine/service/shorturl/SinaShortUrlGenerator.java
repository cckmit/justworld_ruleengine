package com.justworld.custget.ruleengine.service.shorturl;

import com.justworld.custget.ruleengine.dao.BaseConfigDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

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
    @Autowired
    private BaseConfigDAO baseConfigDAO;

    @Override
    public void convertShortUrl(Map<String, String> urlMap) {

        String token = baseConfigDAO.selectByPrimaryKey("short-url-server.sina","token").getCfgValue();
        //如果没有token，则发提醒短信(一天一次) TODO
        log.trace("token={}",token);
        //发送请求
        StringBuilder url = new StringBuilder(serverUrl + "?access_token={1}").append(urlMap.keySet().stream().reduce("",(k1, k2) -> k1.concat("&url_long=" + k2)));
        Map result = restTemplate.getForObject(url.toString(), Map.class, token);

        log.debug("short urls = {}", result);
        for (Map<String, ?> urls : (List<Map<String, ?>>) result.get("urls")) {
            urlMap.put(urls.get("url_long") + "", urls.get("url_short") + "");
        }
    }
}
