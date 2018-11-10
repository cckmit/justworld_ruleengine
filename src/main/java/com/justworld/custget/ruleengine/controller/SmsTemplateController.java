package com.justworld.custget.ruleengine.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.justworld.custget.ruleengine.common.BaseResult;
import com.justworld.custget.ruleengine.dao.SmsTemplateDAO;
import com.justworld.custget.ruleengine.service.bo.SmsTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 基础信息
 */
@Slf4j
@Controller
@RequestMapping(value = "/smsTemplate")
@CrossOrigin
public class SmsTemplateController {

    @Autowired
    private SmsTemplateDAO smsTemplateDAO;


    @ResponseBody
    @PostMapping(value = "/queryList/{pageNo}/{pageSize}")
    public BaseResult<PageInfo<SmsTemplate>> queryList(@PathVariable("pageNo") int pageNo, @PathVariable("pageSize")int pageSize, @RequestBody SmsTemplate cond) {

        return BaseResult.build((t) -> {
            PageHelper.startPage(pageNo,pageSize);
            return new PageInfo<>(smsTemplateDAO.queryList(t));
        }, cond);
    }

    @PostMapping(value = "/update")
    @ResponseBody
    public BaseResult update(@RequestBody SmsTemplate cond) {
        return BaseResult.build((t) -> smsTemplateDAO.updateByPrimaryKeySelective(t), cond);
    }

    @PostMapping(value = "/add")
    @ResponseBody
    public BaseResult<SmsTemplate> add(@RequestBody SmsTemplate cond) {
        return BaseResult.build((t) -> {
            smsTemplateDAO.insert(t);
            return t;
            }, cond);
    }

    @PostMapping(value = "/batchDelete")
    @ResponseBody
    public BaseResult add(@RequestBody List<String> ids) {
        return BaseResult.build((t) -> {
            smsTemplateDAO.batchDelete(t);
            return t;
            }, ids);
    }


}