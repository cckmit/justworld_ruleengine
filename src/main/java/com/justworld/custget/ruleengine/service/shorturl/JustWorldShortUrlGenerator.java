package com.justworld.custget.ruleengine.service.shorturl;

import com.justworld.custget.ruleengine.dao.BaseConfigDAO;
import com.justworld.custget.ruleengine.dao.ShortUrlDefineDAO;
import com.justworld.custget.ruleengine.service.bo.BaseConfig;
import com.justworld.custget.ruleengine.service.bo.ShortUrlDefine;
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
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 新浪短链接生成器
 */
@Slf4j
@Service
public class JustWorldShortUrlGenerator implements IShortUrlGenerator {

    @Autowired
    private ShortUrlDefineDAO shortUrlDefineDAO;
    @Autowired
    private BaseConfigDAO baseConfigDAO;

    @Override
    public void convertShortUrl(Map<String, String> urlMap, Consumer<Map<String,String>> consumer) {

        String shortUrlPrefix = baseConfigDAO.selectByPrimaryKey("SHORT_URL_CONFIG","PREFIX").getCfgValue();

        for (String longUrl : urlMap.keySet()) {
            ShortUrlDefine shortUrlDefine = new ShortUrlDefine();
            shortUrlDefine.setCreateTime(new Date());
            shortUrlDefine.setLongUrl(longUrl);
            shortUrlDefine.setServiceType(urlMap.get(longUrl));
            shortUrlDefineDAO.insert(shortUrlDefine);
            urlMap.put(longUrl, shortUrlPrefix+ShortUrlConveter.encode(shortUrlDefine.getId(), 6));
        }
        consumer.accept(urlMap);
    }

}
