package com.justworld.custget.ruleengine.controller;

import com.justworld.custget.ruleengine.dao.AiSmsJobDAO;
import com.justworld.custget.ruleengine.service.bo.AiSmsJob;
import com.justworld.custget.ruleengine.service.shorturl.SinaShortUrlGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

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
    private SinaShortUrlGenerator sinaShortUrlGenerator;

    @ResponseBody
    @GetMapping(value = "queryList")
    public List<AiSmsJob> queryJobList(){
        log.debug("发送消息list");
        return aiSmsJobDAO.queryList();
    }

    @GetMapping(value = "weibocode")
    public String getSinaAuth(String code){
        sinaShortUrlGenerator.getToken(code);
        return "successs";
    }


}