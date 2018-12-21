package com.justworld.custget.intf.controller;

import com.justworld.custget.ruleengine.dao.AiSmsJobDAO;
import com.justworld.custget.ruleengine.dao.SendSmsDAO;
import com.justworld.custget.ruleengine.dao.SmsClickLogDAO;
import com.justworld.custget.ruleengine.service.bo.AiSmsJob;
import com.justworld.custget.ruleengine.service.bo.SendSms;
import com.justworld.custget.ruleengine.service.bo.SmsClickLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

import java.util.Date;

/**
 * AI短信控制器
 */
@Slf4j
@Controller
@RequestMapping(value = "/click")
public class SmsClickController {

    @Autowired
    private AiSmsJobDAO aiSmsJobDAO;
    @Autowired
    private SmsClickLogDAO smsClickLogDAO;
    @Autowired
    private SendSmsDAO sendSmsDAO;

    @GetMapping(value = "/aismsjob/{aiSmsJobId}")
    public Mono<String> clickAiSmsJob(@PathVariable String aiSmsJobId){
        log.debug("click aismsjob.id={}",aiSmsJobId);
        return Mono.just(aiSmsJobId).flatMap(id -> {
            AiSmsJob aiSmsJob = aiSmsJobDAO.lockByPrimaryKey(Integer.valueOf(id));
            if(aiSmsJob.getSendSmsId()==null){
                log.debug("该短信{}还开始发送，不记录点击量",id);
                return Mono.empty();
            }
            SendSms sendSms = sendSmsDAO.load(Long.valueOf(aiSmsJob.getSendSmsId()));
            if(sendSms==null||sendSms.getStatus()==0){
                log.debug("该短信{}还未发送，不记录点击量",id);
                return Mono.empty();
            }
            sendSms.setClickCount(sendSms.getClickCount()==null?1:(sendSms.getClickCount()+1));
            sendSms.setClickTime(new Date());
            SmsClickLog smsClickLog = new SmsClickLog(null,sendSms.getClickTime(),sendSms.getId());
            sendSmsDAO.updateClickInfo(sendSms);
            smsClickLogDAO.insert(smsClickLog);

            //重定向
            String url = sendSms.getUrl();
            if(!url.startsWith("http")){
                url = "http://"+url;
            }
            return Mono.just("redirect:"+url);
        });

    }
}