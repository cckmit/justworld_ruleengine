package com.justworld.custget.ruleengine.service.shorturl;

import com.justworld.custget.ruleengine.dao.BaseConfigDAO;
import com.justworld.custget.ruleengine.service.bo.BaseConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
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
    @Value("${short-url-server.sina.auth_token_url}")
    private String sinaTokenUrl;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private BaseConfigDAO baseConfigDAO;

    @Override
    public void convertShortUrl(Map<String, String> urlMap) {

        String token = baseConfigDAO.selectByPrimaryKey("short-url-server.sina","token").getCfgValue();

        //发送请求
        StringBuilder url = new StringBuilder(serverUrl + "?access_token={1}").append(urlMap.keySet().stream().reduce("",(k1, k2) -> k1.concat("&url_long=" + k2)));
        try {
            Map result = restTemplate.getForObject(url.toString(), Map.class, token);

            log.debug("short urls = {}", result);
            for (Map<String, ?> urls : (List<Map<String, ?>>) result.get("urls")) {
                urlMap.put(urls.get("url_long") + "", urls.get("url_short") + "");
            }
        } catch (HttpClientErrorException e){
            if(e.getStatusCode().value()==403){
                log.error("授权已过期");
            } else {
                throw e;
            }
        }
    }

    /**
     * 获取新浪API令牌，并入库
     * @param code
     */
    public void getToken(String code){
        //获取新浪短链配置
        BaseConfig appKeyCfg = baseConfigDAO.selectByPrimaryKey("short-url-server.sina","app-key");
        BaseConfig secretCfg = baseConfigDAO.selectByPrimaryKey("short-url-server.sina","app-secret");
        BaseConfig serverUrlCfg = baseConfigDAO.selectByPrimaryKey("short-url-server.sina","redirect-url");
        //用code获取token
        //发送请求
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("client_id", appKeyCfg.getCfgValue());
        map.add("client_secret", secretCfg.getCfgValue());
        map.add("grant_type", "authorization_code");
        map.add("redirect_uri", serverUrlCfg.getCfgValue());
        map.add("code", code);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        Map result = restTemplate.postForObject(sinaTokenUrl, request,Map.class);

        log.trace("result={}",result);
        BaseConfig sinaTokenCfg = baseConfigDAO.selectByPrimaryKey("short-url-server.sina","token");
        sinaTokenCfg.setCfgValue(result.get("access_token")+"");
        baseConfigDAO.updateByPrimaryKey(sinaTokenCfg);
    }

}
