package com.justworld.custget.intf.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.justworld.custget.intf.common.BaseRequest;
import com.justworld.custget.intf.common.RequestHead;
import com.justworld.custget.ruleengine.common.BaseResult;
import com.justworld.custget.ruleengine.dao.SmsJobUserDAO;
import com.justworld.custget.ruleengine.exceptions.RtcdExcception;
import com.justworld.custget.ruleengine.service.bo.AiSmsJob;
import com.justworld.custget.ruleengine.service.bo.SmsJobUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.*;

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
    private SmsJobUserDAO smsJobUserDAO;

    @ResponseBody
    @PostMapping(value = "/addAiSms")
    public BaseResult addAiSms(@RequestBody BaseRequest<AiSmsJob> request) {

        return BaseResult.build(req -> {
            RequestHead reqHead = req.getHead();
            //校验用户密码
            SmsJobUser user = smsJobUserDAO.selectByUsername("1", reqHead.getUsername());
            if (!user.getPassword().equals(reqHead.getPassword())) {
                throw new RtcdExcception("0001", "用户密码不正确");
            }

            ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send("ai_message", req.getBody());
            future.addCallback(o -> log.trace("message sent to " + o.getRecordMetadata().topic() + ", partition " + o.getRecordMetadata().partition() + ", offset " + o.getRecordMetadata().offset()), throwable -> log.trace("消息发送失败：" + throwable.getMessage()));
            return null;
        }, request);
    }
}