package com.justworld.custget.ruleengine.service.shorturl;

import com.justworld.custget.ruleengine.dao.BaseConfigDAO;
import com.justworld.custget.ruleengine.service.bo.BaseConfig;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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
    private BaseConfigDAO baseConfigDAO;

    @Override
    public void convertShortUrl(Map<String, String> urlMap, Consumer<Map<String,String>> consumer) {

        String token = baseConfigDAO.selectByPrimaryKey("short-url-server.sina", "token").getCfgValue();

        //发送请求
        StringBuilder url = new StringBuilder(serverUrl + "?access_token=").append(token).append(urlMap.keySet().stream().reduce("", (k1, k2) -> k1.concat("&url_long=" + k2)));

        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(options -> options.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000).compression(true).afterNettyContextInit(ctx -> {
            ctx.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
        }));

        WebClient webClient = WebClient.builder()
                .clientConnector(connector)
                .baseUrl(url.toString())
                .build();

        Mono<Map> resultMapMono = webClient.get().retrieve().bodyToMono(Map.class);

        resultMapMono.subscribe(result -> {
            log.debug("short urls = {}", result);
            for (Map<String, ?> urls : (List<Map<String, ?>>) result.get("urls")) {
                urlMap.put(urls.get("url_long") + "", urls.get("url_short") + "");
            }
            consumer.accept(urlMap);

        });
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

        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(options -> options.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000).compression(true).afterNettyContextInit(ctx -> {
            ctx.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
        }));

        WebClient webClient = WebClient.builder()
                .clientConnector(connector)
                .baseUrl(sinaTokenUrl)
                .build();

        Mono<Map> resultMapMono = webClient.post().syncBody(map).retrieve().bodyToMono(Map.class);

        Map result = resultMapMono.block();

        log.trace("result={}",result);
        BaseConfig sinaTokenCfg = baseConfigDAO.selectByPrimaryKey("short-url-server.sina","token");
        sinaTokenCfg.setCfgValue(result.get("access_token")+"");
        baseConfigDAO.updateByPrimaryKey(sinaTokenCfg);
    }

}
