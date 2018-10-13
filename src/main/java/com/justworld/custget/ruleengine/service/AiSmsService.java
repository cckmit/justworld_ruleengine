package com.justworld.custget.ruleengine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.justworld.custget.ruleengine.dao.AiSmsJobDAO;
import com.justworld.custget.ruleengine.service.bo.AiSmsJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class AiSmsService {
    @Autowired
    private AiSmsJobDAO aiSmsJobDAO;
    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "ai_message")
    public void handleAiMessage(String message){

        try {
            log.trace("收到ai挂机消息{}",message);
            AiSmsJob aiSmsJob = objectMapper.readValue(message, AiSmsJob.class);
            aiSmsJobDAO.insert(aiSmsJob);
            log.trace("消息入库成功");
        } catch (IOException e) {
            log.error("错误：",e);
        }
    }


}
