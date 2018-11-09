package com.justworld.custget.ruleengine.controller;

import com.justworld.custget.ruleengine.common.BaseResult;
import com.justworld.custget.ruleengine.dao.AiSmsRuleDAO;
import com.justworld.custget.ruleengine.dao.DicDAO;
import com.justworld.custget.ruleengine.dao.RegionDAO;
import com.justworld.custget.ruleengine.exceptions.RtcdExcception;
import com.justworld.custget.ruleengine.service.bo.AiSmsRule;
import com.justworld.custget.ruleengine.service.bo.Dic;
import com.justworld.custget.ruleengine.service.bo.Region;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 基础信息
 */
@Slf4j
@Controller
@RequestMapping(value = "/aismsrule")
@CrossOrigin
public class SmsRuleController {

    @Autowired
    private AiSmsRuleDAO aiSmsRuleDAO;


    @ResponseBody
    @PostMapping(value = "/queryList")
    public BaseResult<List<AiSmsRule>> queryList(@RequestBody AiSmsRule rule) {

        return BaseResult.build((t) -> aiSmsRuleDAO.queryValidAiSmsRuleList(t.getRuleType(),t.getRuleKey()), rule);
    }

    @PostMapping(value = "/update")
    @ResponseBody
    public BaseResult<List<Dic>> update(@RequestBody List<AiSmsRule> aiSmsRuleList) {
        return BaseResult.build(p -> {
            if(CollectionUtils.isEmpty(p)){
                throw new RtcdExcception("999","发送渠道不能清空");
            }
            //删除原来的，增加新配置的
            aiSmsRuleDAO.deleteRule(p.get(0).getRuleType(),p.get(0).getRuleKey());
            //新增
            for (AiSmsRule aiSmsRule : p) {
                aiSmsRule.setStatus("1");
                aiSmsRuleDAO.insert(aiSmsRule);
            }
            return null;
        }, aiSmsRuleList);
    }


}