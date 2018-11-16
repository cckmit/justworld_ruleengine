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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
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
    private ObjectMapper objectMapper;
    @Autowired
    private SmsJobUserDAO smsJobUserDAO;

    @ResponseBody
    @PostMapping(value = "/addAiSms")
    public BaseResult addAiSms(@RequestBody BaseRequest<AiSmsJob> request){

        return BaseResult.build(req->{
            RequestHead reqHead = req.getHead();
            //校验用户密码
            SmsJobUser user = smsJobUserDAO.selectByUsername("1",reqHead.getUsername());
            if(!user.getPassword().equals(DigestUtils.md5DigestAsHex(reqHead.getPassword().getBytes()).toUpperCase())){
                throw new RtcdExcception("0001","用户密码不正确");
            }

            String message = null;
            try {
                message = objectMapper.writeValueAsString(req.getBody());
                ListenableFuture future = kafkaTemplate.send("ai_message", message);
                future.addCallback(o -> log.trace("send-消息发送成功：" + o), throwable -> log.trace("消息发送失败：" + throwable.getMessage()));
            } catch (JsonProcessingException e) {
                throw new RtcdExcception("9999","数据对象转换字符串失败");
            }
            return null;
        },request);
    }
}