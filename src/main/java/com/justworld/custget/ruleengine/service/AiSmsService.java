package com.justworld.custget.ruleengine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.justworld.custget.ruleengine.dao.AiSmsJobDAO;
import com.justworld.custget.ruleengine.dao.PhoneIdentifyDAO;
import com.justworld.custget.ruleengine.dao.SmsTemplateDAO;
import com.justworld.custget.ruleengine.service.bo.AiSmsJob;
import com.justworld.custget.ruleengine.service.bo.PhoneIdentify;
import com.justworld.custget.ruleengine.service.bo.SendSms;
import com.justworld.custget.ruleengine.service.bo.SmsTemplate;
import com.justworld.custget.ruleengine.service.phoneidentify.IPhoneIdentifier;
import com.justworld.custget.ruleengine.service.phoneidentify.PhoneIdentifierFactory;
import com.justworld.custget.ruleengine.service.shorturl.IShortUrlGenerator;
import com.justworld.custget.ruleengine.service.shorturl.ShortUrlGeneratorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AiSmsService {
    @Autowired
    private AiSmsJobDAO aiSmsJobDAO;
    @Autowired
    private PhoneIdentifyDAO phoneIdentifyDAO;
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

    /**
     * 处理接收的AI挂机短信
     * @param message
     */
    @KafkaListener(topics = "ai_message")
    public void handleReceiveAiSms(String message){

        try {
            log.trace("收到ai挂机消息{}",message);
            AiSmsJob aiSmsJob = objectMapper.readValue(message, AiSmsJob.class);

            //手机号识别
            PhoneIdentify identify = phoneIdentifyDAO.selectByPrimaryKey(aiSmsJob.getPhone());
            if(identify == null||!identify.getStatus().equals("1")){
                //手机号未识别过
                aiSmsJob.setPhoneStatus("1");
            } else{
                aiSmsJob.setPhoneStatus("2");
            }

            aiSmsJob.setShortUrlStatus("1");
            aiSmsJob.setStatus("1");
            aiSmsJob.setCreateTime(new Date());
            aiSmsJob.setClickCount(0);
            aiSmsJobDAO.insert(aiSmsJob);
            log.trace("消息入库成功");

            //发送识别消息
            if(aiSmsJob.getPhoneStatus().equals("1")){
                ListenableFuture future = kafkaTemplate.send("phone_identify",aiSmsJob.getId());
                future.addCallback(o -> log.debug("挂机短信任务{}手机号识别消息发送成功：", o), throwable -> log.error("手机号识别消息发送失败",throwable));
            }

            //发送短链接处理消息
            ListenableFuture future = kafkaTemplate.send("short_url_handle",aiSmsJob.getId());
            future.addCallback(o -> log.debug("短信任务{}短链接处理消息发送成功：", o), throwable -> log.error("短链接处理消息发送失败",throwable));
        } catch (IOException e) {
            log.error("错误：",e);
        }
    }

    /**
     * 处理手机号识别
     * @param message
     */
    @Transactional
    @KafkaListener(topics = "phone_identify")
    public void handlePhoneIdentifyMessage(String message){

        AiSmsJob aiSmsJob = aiSmsJobDAO.selectByPrimaryKey(message);

        String phone = aiSmsJob.getPhone();
        PhoneIdentify identify = phoneIdentifyDAO.selectByPrimaryKey(phone);
        if(identify == null||!identify.getStatus().equals("1")){
            IPhoneIdentifier phoneIdentifier = phoneIdentifierFactory.getIdentifier();
            identify = phoneIdentifier.identify(phone);
            if(identify.getStatus()==null){
                identify.setStatus("1");
                phoneIdentifyDAO.insert(identify);
            }else{
                identify.setStatus("1");
                phoneIdentifyDAO.updateByPrimaryKey(identify);
            }
            log.trace("手机号识别完成");
            aiSmsJob.setPhoneStatus("2");
            aiSmsJobDAO.updateByPrimaryKey(aiSmsJob);
        }

        if(aiSmsJob.getShortUrlStatus().equals("2")){   //短链接已生成
            log.trace("短链接已生成，直接插入短信");
            //生成短信
            sendSms(aiSmsJob);
        }


    }

    /**
     * 短链接处理消息
     * @param message
     */
    @Transactional
    @KafkaListener(topics = "short_url_handle")
    public void handleShortUrlMessage(String message){
        AiSmsJob aiSmsJob = aiSmsJobDAO.selectByPrimaryKey(message);

        if(aiSmsJob.getShortUrlStatus().equals("1")) {

            //查询要处理的短信模板
            SmsTemplate smsTemplate = smsTemplateDAO.selectByPrimaryKey(aiSmsJob.getSmsTemplateId());

            //分析链接
            String longUrl = StringUtils.substringsBetween(smsTemplate.getContent(), "<<", ">>")[0];
            log.trace("短信模板中的长链接为:" + longUrl);
            //获取短链
            IShortUrlGenerator generator = shortUrlGeneratorFactory.getGenerator();
            Map<String, String> map = new HashMap<>();
            map.put(longUrl, null);
            generator.convertShortUrl(map);
            aiSmsJob.setSmsTemplateUrl(longUrl);
            aiSmsJob.setSmsShortUrl(map.get(longUrl));
            log.trace("生成的短链接为" + aiSmsJob.getSmsShortUrl());

            aiSmsJob.setShortUrlStatus("2");
            aiSmsJobDAO.updateByPrimaryKey(aiSmsJob);
        }

        //如果号码识别完成，则插入短信
        if(aiSmsJob.getPhoneStatus().equals("2")){   //号码已识别
            log.trace("短链接已生成，直接插入短信");
            //生成短信
            sendSms(aiSmsJob);
        }

    }


    /**
     * 插入待发送短信
     * @param aiSmsJob
     */
    public void sendSms(AiSmsJob aiSmsJob){
        SmsTemplate smsTemplate = smsTemplateDAO.selectByPrimaryKey(aiSmsJob.getSmsTemplateId());

        //组装短信内容
        String smsContent = StringUtils.replace(smsTemplate.getContent(),"<<"+aiSmsJob.getSmsTemplateUrl()+">>",aiSmsJob.getSmsShortUrl());

        SendSms sms = new SendSms();

    }


}
