package com.justworld.custget.ruleengine.controller;

import com.justworld.custget.ruleengine.dao.AiSmsJobDAO;
import com.justworld.custget.ruleengine.service.bo.AiSmsJob;
import com.justworld.custget.ruleengine.service.shorturl.IShortUrlGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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


    @Autowired
    private IShortUrlGenerator sinaShortUrlGenerator;

    @ResponseBody
    @GetMapping(value = "queryList")
    public List<AiSmsJob> queryJobList(){
        log.debug("发送消息list");
        return aiSmsJobDAO.queryList();
    }

    @GetMapping(value = "test/{url}")
    public String test(@PathVariable String url){
        Map<String, String> urlMap = new HashMap<>();
        urlMap.put("http://115.28.235.146:6010/fantds/", null);
        sinaShortUrlGenerator.convertShortUrl(urlMap);
        return urlMap.get(url);
    }
}