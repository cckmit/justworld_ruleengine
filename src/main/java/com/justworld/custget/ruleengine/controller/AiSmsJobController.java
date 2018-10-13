package com.justworld.custget.ruleengine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.justworld.custget.ruleengine.dao.AiSmsJobDAO;
import com.justworld.custget.ruleengine.service.AiSmsService;
import com.justworld.custget.ruleengine.service.bo.AiSmsJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI短信控制器
 */
@Slf4j
@Controller
@RequestMapping(value = "/aismsjob")
public class AiSmsJobController {

    @Autowired
    private AiSmsJobDAO aiSmsJobDAO;

    @ResponseBody
    @GetMapping(value = "queryList")
    public List<AiSmsJob> queryJobList(){
        log.debug("发送消息list");
        return aiSmsJobDAO.queryList();
    }
}