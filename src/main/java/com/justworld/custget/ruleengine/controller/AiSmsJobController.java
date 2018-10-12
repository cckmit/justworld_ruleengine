package com.justworld.custget.ruleengine.controller;

import com.justworld.custget.ruleengine.dao.AiSmsJobDAO;
import com.justworld.custget.ruleengine.service.bo.AiSmsJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI短信控制器
 */
@Controller
@RequestMapping(value = "/aismsjob")
public class AiSmsJobController {

    @Autowired
    private AiSmsJobDAO aiSmsJobDAO;

    @ResponseBody
    @PostMapping(value = "add")
    public Map<String,String> addAiSms(@RequestBody AiSmsJob aiSmsJob){
        try{
            aiSmsJobDAO.insert(aiSmsJob);
            return buildResult(true,null,null);
        } catch (Exception e){
            return buildResult(false,"999",e.getMessage());
        }
    }

    @ResponseBody
    @GetMapping(value = "queryList")
    public List<AiSmsJob> queryJobList(){
        return aiSmsJobDAO.queryList();
    }

    protected Map<String,String> buildResult(boolean isSuccess, String errorCode, String errorMsg){
        Map<String,String> resultMap = new HashMap<>();
        if(isSuccess){
            resultMap.put("rtcd","0");
        }else{
            resultMap.put("rtcd",errorCode);
            resultMap.put("msg",errorMsg);
        }
        return resultMap;
    }
}