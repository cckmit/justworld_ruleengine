package com.justworld.custget.intf.controller;

import com.justworld.custget.ruleengine.dao.AiSmsJobDAO;
import com.justworld.custget.ruleengine.dao.ShortUrlDefineDAO;
import com.justworld.custget.ruleengine.service.bo.AiSmsJob;
import com.justworld.custget.ruleengine.service.bo.ShortUrlDefine;
import com.justworld.custget.ruleengine.service.shorturl.ShortUrlConveter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;

/**
 * AI短信控制器
 */
@Slf4j
@Controller
@RequestMapping(value = "")
public class ShortUrlController {

    @Autowired
    private ShortUrlDefineDAO shortUrlDefineDAO;

    @GetMapping(value = "/{shortUrl}")
    public Mono<String> clickAiSmsJob(@PathVariable String shortUrl){
        log.trace("receive short url={}",shortUrl);
        return Mono.just(shortUrl).flatMap(shortCode -> {
            Long id = ShortUrlConveter.decode(shortCode);
            ShortUrlDefine shortUrlDefine = shortUrlDefineDAO.selectByPrimaryKey(id);

            //重定向
            String url = shortUrlDefine.getLongUrl();
            if(!url.startsWith("http")&&!url.startsWith("https")){
                url = "http://"+url;
            }
            return Mono.just("redirect:"+url);
        });

    }
}