package com.justworld.custget.ruleengine.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.justworld.custget.ruleengine.common.BaseResult;
import com.justworld.custget.ruleengine.dao.AiSmsJobDAO;
import com.justworld.custget.ruleengine.service.bo.AiSmsJob;
import com.justworld.custget.ruleengine.service.shorturl.SinaShortUrlGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * AI短信控制器
 */
@Slf4j
@Controller
@RequestMapping(value = "/aismsjob")
@CrossOrigin
public class AiSmsJobController {

    @Autowired
    private AiSmsJobDAO aiSmsJobDAO;

    @Autowired
    private SinaShortUrlGenerator sinaShortUrlGenerator;

    @ResponseBody
    @PostMapping(value = "/queryList/{pageNo}/{pageSize}")
    @PreAuthorize("hasAuthority('1')")
    public BaseResult<PageInfo<AiSmsJob>> queryJobList(@PathVariable("pageNo") int pageNo, @PathVariable("pageSize")int pageSize, @RequestBody AiSmsJob cond){

        PageHelper.startPage(pageNo,pageSize);
        return BaseResult.buildSuccess(new PageInfo<>(aiSmsJobDAO.queryList(cond)));
    }

    @GetMapping(value = "weibocode")
    @ResponseBody
    public String getSinaAuth(String code){
        sinaShortUrlGenerator.getToken(code);
        return "successs";
    }


}