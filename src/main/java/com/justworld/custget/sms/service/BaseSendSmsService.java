package com.justworld.custget.sms.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.justworld.custget.ruleengine.dao.AiSmsJobDAO;
import com.justworld.custget.ruleengine.dao.BaseConfigDAO;
import com.justworld.custget.ruleengine.dao.NotifyDAO;
import com.justworld.custget.ruleengine.dao.SendSmsDAO;
import com.justworld.custget.ruleengine.service.bo.Notify;
import com.justworld.custget.ruleengine.service.bo.SendSms;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 万众联动
 */
@Slf4j
@Service
public abstract class BaseSendSmsService {

    @Autowired
    protected SendSmsDAO sendSmsDAO;
    @Autowired
    protected BaseConfigDAO baseConfigDAO;
    @Autowired
    protected NotifyDAO notifyDAO;
    @Autowired
    protected AiSmsJobDAO aiSmsJobDAO;

    public void sendDbSms() {
        while (true) {
            //锁定任务
            String lockId = UUID.randomUUID().toString();
            int lockCount = sendSmsDAO.lockSendSms(getDispatcherId(), lockId, 100);
            if (lockCount == 0) {
                return;
            }
            sendSms(lockId);

        }
    }

    public void sendQueueSms(List<SendSms> sendSmsList) {
        //锁定任务
        String lockId = UUID.randomUUID().toString();
        int lockCount = sendSmsDAO.lockSendSmsByIds(getDispatcherId(), lockId, sendSmsList);
        if (lockCount == 0) {
            return;
        }
        sendSms(lockId);

    }

    private final void sendSms(String lockId) {

        SendSms sendSmsUpdate = new SendSms();
        sendSmsUpdate.setDispatcherId(getDispatcherId());
        sendSmsUpdate.setLockId(lockId);

        try {
            sendSms(sendSmsDAO.queryLockedSendSmsList(getDispatcherId(), lockId));
        } catch (Exception e) {
            log.error("短信渠道发送超时", e);
            //发通知
            Notify errorNotify = Notify.createDispatcherNotify(sendSmsUpdate.getLockId(), "本渠道短信发送出现故障:" + e.getMessage());
            notifyDAO.insert(errorNotify);

            sendSmsUpdate.setStatus(0);
            sendSmsUpdate.setRetryTimes(1);
            sendSmsDAO.updateAndUnLockSendSms(sendSmsUpdate);

        }
    }

    abstract protected void sendSms(List<SendSms> sendSms);

    /**
     * 获取渠道ID
     * @return
     */
    abstract public String getDispatcherId();
}
