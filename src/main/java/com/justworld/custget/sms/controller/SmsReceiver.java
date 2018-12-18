package com.justworld.custget.sms.controller;

import com.justworld.custget.ruleengine.common.BaseResult;
import com.justworld.custget.ruleengine.dao.SmsDispatcherDAO;
import com.justworld.custget.ruleengine.service.bo.SmsDispatcher;
import com.justworld.custget.sms.service.UESendSmsServiceFluxLTCredit;
import com.justworld.custget.sms.service.ZDSendSmsServiceFlux;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * 短信发送器
 */
@Slf4j
@Controller
@RequestMapping(value = "/smsReceive")
@CrossOrigin
public class SmsReceiver {

    @Autowired
    private UESendSmsServiceFluxLTCredit ueSmsReceiver;

    @Autowired
    private ZDSendSmsServiceFlux zdSendSmsServiceFlux;

    @PostMapping(value = "/UE")
    @ResponseBody
    public Mono<String> receiveUE(ServerWebExchange serverWebExchange){
        Mono<MultiValueMap<String,String>> reqMap = serverWebExchange.getFormData();
        return reqMap.flatMap(req-> ueSmsReceiver.receiveSms(req.get("pushtype").get(0),req.get("data").get(0)));
    }

    @PostMapping(value = "/ZDReport")
    @ResponseBody
    public Mono<String> receiveZDReport(ServerWebExchange serverWebExchange){
        Mono<MultiValueMap<String,String>> reqMap = serverWebExchange.getFormData();
        return reqMap.flatMap(req-> zdSendSmsServiceFlux.receiveSmsReport(req.get("data").get(0)));
    }

}