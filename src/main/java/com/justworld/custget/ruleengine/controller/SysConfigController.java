package com.justworld.custget.ruleengine.controller;

import com.justworld.custget.ruleengine.common.BaseResult;
import com.justworld.custget.ruleengine.dao.BaseConfigDAO;
import com.justworld.custget.ruleengine.service.bo.BaseConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统配置管理控制器
 */
@Slf4j
@Controller
@RequestMapping(value = "/sysconfig")
@CrossOrigin
public class SysConfigController {
    @Autowired
    private BaseConfigDAO baseConfigDAO;

    @ResponseBody
    @PostMapping("/queryList")
    public BaseResult<List<BaseConfig>> queryList(String cfgGroup){
        try {
            List<BaseConfig> sinaConfigList = baseConfigDAO.queryGroup(cfgGroup);
            return BaseResult.buildSuccess(sinaConfigList);
        } catch (Exception e){
            log.error("出现错误:",e);
            return BaseResult.buildFail("9999",e.getMessage());
        }

    }

    @ResponseBody
    @PostMapping("/saveBaseCfgValue")
    public BaseResult saveCfgValue(@RequestBody BaseConfig cfg){
        try {
            baseConfigDAO.updateByPrimaryKey(cfg);
            return BaseResult.buildSuccess();
        } catch (Exception e){
            log.error("出现错误:",e);
            return BaseResult.buildFail("9999",e.getMessage());
        }
    }
}
