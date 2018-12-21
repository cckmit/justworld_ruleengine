package com.justworld.custget.ruleengine.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.justworld.custget.ruleengine.common.BaseResult;
import com.justworld.custget.ruleengine.dao.SmsTemplateDAO;
import com.justworld.custget.ruleengine.service.bo.SmsTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
        return BaseResult.build((t) -> {
            String url = StringUtils.substringsBetween(t.getContent(), "<<", ">>")[0];
            t.setUrl(url);
            return smsTemplateDAO.updateByPrimaryKeySelective(t);
            }, cond);
    }

    @PostMapping(value = "/add")
    @ResponseBody
    public BaseResult<SmsTemplate> add(@RequestBody SmsTemplate cond) {
        return BaseResult.build((t) -> {
            String url = StringUtils.substringsBetween(t.getContent(), "<<", ">>")[0];
            t.setUrl(url);
            smsTemplateDAO.insert(t);
            return t;
            }, cond);
    }

    @PostMapping(value = "/batchDelete")
    @ResponseBody
    public BaseResult batchDelete(@RequestBody List<String> ids) {
        return BaseResult.build((t) -> {
            smsTemplateDAO.batchDelete(t);
            return t;
            }, ids);
    }


}