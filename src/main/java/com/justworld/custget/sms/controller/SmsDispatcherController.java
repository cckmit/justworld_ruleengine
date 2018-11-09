package com.justworld.custget.sms.controller;

import com.justworld.custget.ruleengine.common.BaseResult;
import com.justworld.custget.ruleengine.dao.SmsDispatcherDAO;
import com.justworld.custget.ruleengine.service.bo.SmsDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 短信发送器
 */
@Slf4j
@Controller
@RequestMapping(value = "/smsDispatcher")
@CrossOrigin
public class SmsDispatcherController {

    @Autowired
    private SmsDispatcherDAO smsDispatcherDAO;

    @ResponseBody
    @PostMapping(value = "/queryList")
    public BaseResult<List<SmsDispatcher>> queryJobList(){

        return BaseResult.build((p)->smsDispatcherDAO.queryAllSmsDispatcherList(),null);
    }

}