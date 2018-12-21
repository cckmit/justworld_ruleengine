package com.justworld.custget.ruleengine.service.smsdispatcher;

import com.justworld.custget.ruleengine.dao.AiSmsJobDAO;
import com.justworld.custget.ruleengine.dao.AiSmsRuleDAO;
import com.justworld.custget.ruleengine.dao.SendSmsDAO;
import com.justworld.custget.ruleengine.dao.SmsDispatcherDAO;
import com.justworld.custget.ruleengine.service.bo.AiSmsJob;
import com.justworld.custget.ruleengine.service.bo.AiSmsRule;
import com.justworld.custget.ruleengine.service.bo.SendSms;
import com.justworld.custget.ruleengine.service.bo.SmsDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    @Autowired
    private SendSmsDAO sendSmsDAO;
    @Autowired
    private AiSmsJobDAO aiSmsJobDAO;
    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Scheduled(cron = "50 0/10 * * * *")
    @KafkaListener(topics = "send_sms_notify_decide_dispatcher")
    public void decideDispatcher(){
        String lockId = UUID.randomUUID().toString();
        int lockNum = sendSmsDAO.lockSendSmsForDecideDispatcher("0",lockId,1000);
        if(lockNum<=0){
            return;
        }
        log.debug("本次待分配渠道短信数量={}",lockNum);

        List<SendSms> sendSmsList = sendSmsDAO.queryLockedSendSmsList("0",lockId);
        for (SendSms sendSms : sendSmsList) {
            try {
                //判断短信类型
                if ("1".equals(sendSms.getSmsType())) {
                    //AI挂机短信
                    AiSmsJob aiSmsJob = aiSmsJobDAO.selectBySendSmsId(sendSms.getId());
                    SmsDispatcher dispatcher = select(aiSmsJob);
                    if (dispatcher != null) {
                        sendSms.setDispatcherId(dispatcher.getDispatcherKey());
                        sendSms.setStatus(0);
                        sendSmsDAO.updateDispatcherAndUnlock(sendSms);
                        //发送发短信通知
                        ListenableFuture future = kafkaTemplate.send("send_sms_notify_" + dispatcher.getDispatcherKey(), sendSms);
                        future.addCallback(o -> log.debug("挂机短信任务{}短信发送通知消息发送成功："), throwable -> log.error("短信发送通知消息发送失败", throwable));
                    }
                    //没有渠道的，也不解锁了，30分钟后再重试
                }
            } catch (Exception e){
                log.error("分配短信渠道出错",e);
                //不解锁，处理下一条
                continue;
            }
        }
    }


    /**
     * 选择挂机任务适用的短信渠道
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
