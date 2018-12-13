package com.justworld.custget.intf.controller;

import com.justworld.custget.ruleengine.dao.AiSmsJobDAO;
import com.justworld.custget.ruleengine.service.bo.AiSmsJob;
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
@RequestMapping(value = "/click")
public class SmsClickController {

    @Autowired
    private AiSmsJobDAO aiSmsJobDAO;

    @GetMapping(value = "/aismsjob/{aiSmsJobId}")
    public Mono<String> clickAiSmsJob(@PathVariable String aiSmsJobId){
        log.debug("click aismsjob.id={}",aiSmsJobId);
        return Mono.just(aiSmsJobId).flatMap(id -> {
            AiSmsJob aiSmsJob = aiSmsJobDAO.lockByPrimaryKey(Integer.valueOf(id));
            if(aiSmsJob.getShortUrlStatus().equals("1")){
                log.debug("短链接未生成，不记录点击数");
                return null;
            }
            aiSmsJob.setClickCount(aiSmsJob.getClickCount()+1);
            aiSmsJob.setClickTime(new Timestamp(System.currentTimeMillis()));
            aiSmsJobDAO.updateByPrimaryKey(aiSmsJob);

            //重定向
            String url = aiSmsJob.getSmsTemplateUrl();
            if(!url.startsWith("http")){
                url = "http://"+url;
            }
            return Mono.just("redirect:"+url);
        });

    }
}