package com.justworld.custget.intf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.justworld.custget.ruleengine.service.bo.AiSmsJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

import static com.justworld.custget.intf.common.ResultBuilder.buildResult;

/**
 * AI短信控制器
 */
@Slf4j
@Controller
@RequestMapping(value = "/messageReceive")
public class MessageReceiverController {

    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @ResponseBody
    @PostMapping(value = "/addAiSms")
    public Map<String,String> addAiSms(AiSmsJob aiSmsJob){
        try{
            String message = objectMapper.writeValueAsString(aiSmsJob);
            ListenableFuture future = kafkaTemplate.send("ai_message", message);
            future.addCallback(o -> System.out.println("send-消息发送成功：" + o), throwable -> System.out.println("消息发送失败：" + throwable.getMessage()));
            return buildResult(true,null,null);
        } catch (Exception e){
            return buildResult(false,"999",e.getMessage());
        }
    }
}