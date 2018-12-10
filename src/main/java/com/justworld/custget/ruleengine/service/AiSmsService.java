package com.justworld.custget.ruleengine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.justworld.custget.ruleengine.dao.*;
import com.justworld.custget.ruleengine.service.bo.*;
import com.justworld.custget.ruleengine.service.phoneidentify.IPhoneIdentifier;
import com.justworld.custget.ruleengine.service.phoneidentify.PhoneIdentifierFactory;
import com.justworld.custget.ruleengine.service.phoneidentify.PhoneOperatorIdentify;
import com.justworld.custget.ruleengine.service.shorturl.IShortUrlGenerator;
import com.justworld.custget.ruleengine.service.shorturl.ShortUrlGeneratorFactory;
import com.justworld.custget.ruleengine.service.smsdispatcher.AiSmsDispatcherSelector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiSmsService {
    @Autowired
    private AiSmsJobDAO aiSmsJobDAO;
    @Autowired
    private PhoneIdentifyDAO phoneIdentifyDAO;
    @Autowired
    private PhoneSegmentDAO phoneSegmentDAO;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Autowired
    private PhoneIdentifierFactory phoneIdentifierFactory;
    @Autowired
    private ShortUrlGeneratorFactory shortUrlGeneratorFactory;
    @Autowired
    private SmsTemplateDAO smsTemplateDAO;
    @Autowired
    private SendSmsDAO sendSmsDAO;
    @Autowired
    private BaseConfigDAO baseConfigDAO;
    @Autowired
    private AiSmsDispatcherSelector dispatcherSelector;

    /**
     * 处理接收的AI挂机短信
     *
     * @param messageList
     */
    @KafkaListener(topics = "ai_message", containerFactory = "kafkaListenerBatchConsumerFactory")
    public void handleReceiveAiSms(List<AiSmsJob> messageList) {
        log.debug("收到ai挂机消息共{}条", messageList.size());

        List<AiSmsJob> aiSmsJobList = new ArrayList<>();
        for (AiSmsJob aiSmsJob : messageList) {
            aiSmsJob.setPhoneStatus("1");
            aiSmsJob.setShortUrlStatus("1");
            aiSmsJob.setStatus("1");
            aiSmsJob.setCreateTime(new Date());
            aiSmsJob.setClickCount(0);
            aiSmsJobList.add(aiSmsJob);
        }

        aiSmsJobDAO.insert(aiSmsJobList);

        int success = 0;
        for (AiSmsJob aiSmsJob : aiSmsJobList) {
            if (aiSmsJob.getId() == null) {
                log.info("消息重复:{}", aiSmsJob.getAiSeq());
                continue;
            }
            //发送识别消息
            ListenableFuture future = kafkaTemplate.send("phone_identify", aiSmsJob);
            future.addCallback(o -> log.debug("挂机短信任务{}手机号识别消息发送成功：", o), throwable -> log.error("手机号识别消息发送失败", throwable));

            //发送短链接处理消息
            ListenableFuture future2 = kafkaTemplate.send("short_url_handle", aiSmsJob);
            future2.addCallback(o -> log.debug("短信任务{}短链接处理消息发送成功：", o), throwable -> log.error("短链接处理消息发送失败", throwable));

            success++;
        }
        log.debug("消息入库处理完成，本次入库{}", success);
    }

    /**
     * 处理手机号识别
     * @param aiSmsJobList
     */
    @Transactional
    @KafkaListener(topics = "phone_identify", containerFactory = "kafkaListenerSinaShortUrlFactory")
    public void handlePhoneIdentifyMessage(List<AiSmsJob> aiSmsJobList) {

        log.debug("待识别号码数量{}", aiSmsJobList.size());
        for (AiSmsJob aiSmsJob : aiSmsJobList) {
            PhoneIdentify identify = phoneIdentifyDAO.selectByPrimaryKey(aiSmsJob.getPhone());
            if (identify == null) {
                //判断号段是否存在
                PhoneSegment phoneSegment = phoneSegmentDAO.selectByPrimaryKey(aiSmsJob.getPhone().substring(0, 7));
                if (phoneSegment == null || !"1".equals(phoneSegment.getStatus())) {
                    phoneIdentifierFactory.getIdentifier().identify(aiSmsJob.getPhone(), newPhoneSegment -> {
                        identifyPhone(aiSmsJob, phoneSegment);
                        checkIfSend(aiSmsJob.getId());
                    });
                } else {
                    identifyPhone(aiSmsJob, phoneSegment);
                    checkIfSend(aiSmsJob.getId());
                    log.debug("号段{}存在", aiSmsJob.getPhone().substring(0, 7));
                }

            } else {
                //号码已识别过，直接改状态
                aiSmsJob.setPhoneStatus("2");
                aiSmsJobDAO.updatePhoneStatus(aiSmsJob);
                checkIfSend(aiSmsJob.getId());
            }
        }
    }

    private void checkIfSend(Integer aiSmsJobId) {
        AiSmsJob aiSmsJob = aiSmsJobDAO.lockByPrimaryKey(aiSmsJobId);
        if (aiSmsJob.getShortUrlStatus().equals("2") && aiSmsJob.getPhoneStatus().equals("2")) {   //短链接已生成,号码已识别
            log.trace("短链接已生成,号码已识别，直接插入短信");
            //生成短信
            sendSms(aiSmsJob);
        }
    }

    private void identifyPhone(AiSmsJob aiSmsJob, PhoneSegment phoneSegment) {
        PhoneIdentify identify;
        phoneSegmentDAO.insertOrUpdate(phoneSegment);
        identify = new PhoneIdentify();
        identify.setPhone(aiSmsJob.getPhone());
        identify.setCity(phoneSegment.getCity());
        identify.setProvince(phoneSegment.getProvince());
        identify.setTelOperator(phoneSegment.getTelOperator());
        identify.setUpdateTime(new Date());
        phoneIdentifyDAO.insert(identify);

        //更新任务状态
        aiSmsJob.setPhoneStatus("2");
        aiSmsJobDAO.updatePhoneStatus(aiSmsJob);
    }

    /**
     * 短链接处理消息
     *
     * @param message
     */
    @Transactional
    @KafkaListener(topics = "short_url_handle", containerFactory = "kafkaListenerSinaShortUrlFactory")
    public void handleShortUrlMessage(List<AiSmsJob> message) {
        List<AiSmsJob> aiSmsJobList = aiSmsJobDAO.queryListByIds(message.stream().map(AiSmsJob::getId).collect(Collectors.toList()));

        //获取短链
        IShortUrlGenerator generator = shortUrlGeneratorFactory.getGenerator();
        Map<String, String> map = new HashMap<>();
        BaseConfig clickStatCfg = baseConfigDAO.selectByPrimaryKey("aismsjob-config", "click-url");
        for (AiSmsJob aiSmsJob : aiSmsJobList) {
            if (aiSmsJob.getShortUrlStatus().equals("1")) {
                map.put(clickStatCfg.getCfgValue() + "/" + aiSmsJob.getId(), null);
            }
        }

        if (map.size() > 0) {
            //生成短链
            generator.convertShortUrl(map, converMap -> {
                //回写job表
                for (AiSmsJob aiSmsJob : aiSmsJobList) {
                    if (aiSmsJob.getShortUrlStatus().equals("1")) {

                        //分析链接
                        String longUrl = StringUtils.substringsBetween(aiSmsJob.getSmsTemplateContent(), "<<", ">>")[0];
                        log.trace("短信模板中的长链接为:" + longUrl);
                        aiSmsJob = aiSmsJobDAO.lockByPrimaryKey(aiSmsJob.getId());
                        aiSmsJob.setSmsTemplateUrl(longUrl);
                        String replaceUrl = clickStatCfg.getCfgValue() + "/" + aiSmsJob.getId();
                        aiSmsJob.setSmsShortUrl(StringUtils.substringAfter(converMap.get(replaceUrl), "http://"));
                        log.trace("生成的短链接为" + aiSmsJob.getSmsShortUrl());

                        aiSmsJob.setShortUrlStatus("2");
                        aiSmsJobDAO.updateShortUrlStatus(aiSmsJob);
                        checkIfSend(aiSmsJob.getId());
                    }
                }

            });
        }

    }


    /**
     * 插入待发送短信
     *
     * @param aiSmsJob
     */
    public void sendSms(AiSmsJob aiSmsJob) {
        aiSmsJob = aiSmsJobDAO.lockByPrimaryKey(aiSmsJob.getId());
        if (aiSmsJob.getSendSmsId() != null) {
            log.info("该任务已发送短信");
            return;
        }
        SmsTemplate smsTemplate = smsTemplateDAO.selectByPrimaryKey(aiSmsJob.getSmsTemplateId());

        //组装短信内容
        String smsContent = StringUtils.replace(smsTemplate.getContent(), "<<" + aiSmsJob.getSmsTemplateUrl() + ">>", aiSmsJob.getSmsShortUrl());

        //决定使用的渠道
        SmsDispatcher dispatcher = dispatcherSelector.select(aiSmsJob);
        SendSms sms = new SendSms(aiSmsJob.getPhone(), smsContent, dispatcher.getDispatcherKey());
        sendSmsDAO.insert(sms);

        aiSmsJob.setSendSmsId(sms.getId() + "");
        aiSmsJob.setStatus("2");
        aiSmsJobDAO.updateByPrimaryKey(aiSmsJob);

        //发送发短信通知
        ListenableFuture future = kafkaTemplate.send("send_sms_notify_" + dispatcher.getDispatcherKey(), sms);
        future.addCallback(o -> log.debug("挂机短信任务{}短信发送通知消息发送成功："), throwable -> log.error("短信发送通知消息发送失败", throwable));
    }
}
