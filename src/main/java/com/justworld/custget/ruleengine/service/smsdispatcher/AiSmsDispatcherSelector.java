package com.justworld.custget.ruleengine.service.smsdispatcher;

import com.justworld.custget.ruleengine.dao.AiSmsRuleDAO;
import com.justworld.custget.ruleengine.dao.SmsDispatcherDAO;
import com.justworld.custget.ruleengine.service.bo.AiSmsJob;
import com.justworld.custget.ruleengine.service.bo.AiSmsRule;
import com.justworld.custget.ruleengine.service.bo.SmsDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI短信发送渠道选择器
 */
@Slf4j
@Service
public class AiSmsDispatcherSelector {

    @Autowired
    private AiSmsRuleDAO aiSmsRuleDAO;
    @Autowired
    private SmsDispatcherDAO smsDispatcherDAO;

    /**
     * 选择挂机任务适用的短信渠道 TODO
     * @param aiSmsJob
     * @return
     */
    public SmsDispatcher select(AiSmsJob aiSmsJob){

        //查询运营商适用渠道
        List<AiSmsRule> operatorRuleList = aiSmsRuleDAO.queryValidAiSmsRuleList("OPERATOR",aiSmsJob.getTelOperator());

        //查询省份适用渠道
        List<AiSmsRule> provinceRuleList = aiSmsRuleDAO.queryValidAiSmsRuleList("PROVINCE",aiSmsJob.getProvince());

        //查询城市适用渠道
        List<AiSmsRule> cityRuleList = aiSmsRuleDAO.queryValidAiSmsRuleList("CITY",aiSmsJob.getCity());

        //查询AI用户名适用渠道
        List<AiSmsRule> aiUsernammeRuleList = aiSmsRuleDAO.queryValidAiSmsRuleList("AI_USERNAME",aiSmsJob.getAiUsername());

        //计算最终渠道
        List<SmsDispatcher> dispatcherList = smsDispatcherDAO.queryAllSmsDispatcherList();

        //求交集
        if(CollectionUtils.isNotEmpty(operatorRuleList)){
            dispatcherList = dispatcherList.stream().filter(d -> operatorRuleList.stream().anyMatch(p -> p.getDispatcherKey().equals(d.getDispatcherKey()))).collect(Collectors.toList());
        }
        if(CollectionUtils.isNotEmpty(provinceRuleList)){
            dispatcherList = dispatcherList.stream().filter(d -> provinceRuleList.stream().anyMatch(p -> p.getDispatcherKey().equals(d.getDispatcherKey()))).collect(Collectors.toList());
        }
        if(CollectionUtils.isNotEmpty(cityRuleList)){
            dispatcherList = dispatcherList.stream().filter(d -> cityRuleList.stream().anyMatch(p -> p.getDispatcherKey().equals(d.getDispatcherKey()))).collect(Collectors.toList());
        }
        if(CollectionUtils.isNotEmpty(aiUsernammeRuleList)){
            dispatcherList = dispatcherList.stream().filter(d -> aiUsernammeRuleList.stream().anyMatch(p -> p.getDispatcherKey().equals(d.getDispatcherKey()))).collect(Collectors.toList());
        }
        if(CollectionUtils.isEmpty(dispatcherList)){
            log.warn("本任务id={}根据规则无法匹配到任何短信渠道",aiSmsJob.getId());
            return null;
        }else{
            int selectIndex = RandomUtils.nextInt(0,dispatcherList.size()-1);
            SmsDispatcher dispatcher = dispatcherList.get(selectIndex);
            log.trace("本任务{}匹配到的短信渠道共{}条，本次选中{}",aiSmsJob.getId(),dispatcherList.size(),dispatcher.getDispatcherKey());
            return dispatcher;
        }
    }
}
