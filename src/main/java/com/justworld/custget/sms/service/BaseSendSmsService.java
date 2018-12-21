package com.justworld.custget.sms.service;

import com.justworld.custget.ruleengine.dao.*;
import com.justworld.custget.ruleengine.service.bo.Notify;
import com.justworld.custget.ruleengine.service.bo.SendSms;
import com.justworld.custget.ruleengine.service.bo.SmsDispatcher;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 万众联动
 */
@Slf4j
@Service
public abstract class BaseSendSmsService {

    @Autowired
    protected SendSmsDAO sendSmsDAO;
    @Autowired
    protected SmsDispatcherDAO smsDispatcherDAO;
    @Autowired
    protected NotifyDAO notifyDAO;
    @Autowired
    protected AiSmsJobDAO aiSmsJobDAO;
    @Autowired
    protected SendSmsServiceFlux sendSmsServiceFlux;

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
            log.error("短信渠道发送异常", e);
            //发通知
            Notify errorNotify = Notify.createDispatcherNotify(sendSmsUpdate.getLockId(), "本渠道短信发送出现故障:" + e.getMessage());
            notifyDAO.insert(errorNotify);

        }
    }


    final protected void sendSms(List<SendSms> sendSmsList) {

        log.debug("本次短信发送条数:{}", sendSmsList.size());

        SmsDispatcher dispatcher = smsDispatcherDAO.selectByPrimaryKey(getDispatcherId());

        Flux<SendSms> sendSmsOverFlux = Flux.fromIterable(sendSmsList);
        sendSmsServiceFlux.sendSms(dispatcher,sendSmsOverFlux,getRequestBuilder(),handleResult());
    }

    abstract protected Function<Triple<SmsDispatcher, WebClient, SendSms>, Mono<String>> getRequestBuilder();

    abstract protected Function<String, Triple<Integer,String,String>> handleResult();

    abstract protected String getDispatcherId();


}
