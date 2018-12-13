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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
public class WZLDSendSmsService {

    @Autowired
    private SendSmsDAO sendSmsDAO;
    @Autowired
    private BaseConfigDAO baseConfigDAO;
    @Autowired
    private NotifyDAO notifyDAO;
    @Autowired
    private AiSmsJobDAO aiSmsJobDAO;

    private String dispatcherId = "5";

//    @Scheduled(cron = "10 0/30 * * * *")
    public void sendDbSms() {
        while(true) {
            //锁定任务
            String lockId = UUID.randomUUID().toString();
            int lockCount = sendSmsDAO.lockSendSms(dispatcherId, lockId, 100);
            if (lockCount > 0) {
                log.trace("本次万众联动短信渠道批量发送任务共{}条", lockCount);
                sendSms(lockId);
            } else {
                return;
            }
        }

    }

//    @KafkaListener(topics = "send_sms_notify_5", containerFactory = "kafkaListenerBatchConsumerFactory")
    public void sendQueueSms(List<SendSms> sendSmsList) {
        //锁定任务
        String lockId = UUID.randomUUID().toString();
        int lockCount = sendSmsDAO.lockSendSmsByIds(dispatcherId, lockId, sendSmsList);
        if (lockCount > 0) {
            log.trace("本次万众联动短信渠道批量发送队列任务共{}条", lockCount);
        } else {
            return;
        }
        sendSms(lockId);

    }

    public void sendSms(String lockId) {

        SendSms sendSmsUpdate = new SendSms();
        sendSmsUpdate.setDispatcherId(dispatcherId);
        sendSmsUpdate.setLockId(lockId);

        try {
            List<SendSms> sendSmsList = sendSmsDAO.queryLockedSendSmsList(dispatcherId, lockId);
            //查询短信发送的用户名密码
            String sendUrl = baseConfigDAO.selectByPrimaryKey("WANZONGLIANDONG_SMS_CONFIG", "SEND_URL").getCfgValue();
            String account = baseConfigDAO.selectByPrimaryKey("WANZONGLIANDONG_SMS_CONFIG", "ACCOUNT").getCfgValue();
            String password = baseConfigDAO.selectByPrimaryKey("WANZONGLIANDONG_SMS_CONFIG", "PASSWORD").getCfgValue();
            String extno = baseConfigDAO.selectByPrimaryKey("WANZONGLIANDONG_SMS_CONFIG", "EXTNO").getCfgValue();

            ReactorClientHttpConnector connector = new ReactorClientHttpConnector(options -> options.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000).compression(true).afterNettyContextInit(ctx -> {
                ctx.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
            }));

            WebClient webClient = WebClient.builder()
                    .clientConnector(connector)
                    .baseUrl(sendUrl)
                    .build();

            for (SendSms sendSms : sendSmsList) {
                MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                map.add("action", "send");
                map.add("account", account);
                map.add("password", password);
                map.add("extno", extno);

                map.add("mobile", sendSms.getPhone());
                map.add("content", sendSms.getContent());

                send(sendSms, map, webClient);

            }
        } catch (Exception e) {
            log.error("短信渠道发送异常", e);
            //发通知
            Notify errorNotify = Notify.createDispatcherNotify(sendSmsUpdate.getLockId(), "本渠道短信发送出现故障:" + e.getMessage());
            notifyDAO.insert(errorNotify);

            sendSmsUpdate.setStatus(2);
            sendSmsDAO.updateAndUnLockSendSms(sendSmsUpdate);

        }
    }

    @Transactional
    public void send(SendSms sendSms, MultiValueMap<String, String> map, WebClient webClient) {
        Mono<String> result =
                webClient.post()
                        .syncBody(map)
                        .retrieve()
                        .bodyToMono(String.class);

        Mono<SendSms> sms = Mono.just(sendSms);
        Mono.zip(sms, result).subscribe(p -> {
                    SendSms sendSmsUpdate = p.getT1();
                    String r = p.getT2();
                    try {
                        sendSmsUpdate.setRetryTimes(null);
                        log.trace("response xml = {}", r);
                        Map resultMap = new XmlMapper().readValue(r, Map.class);
                        sendSmsUpdate.setSendTime(new Date());
                        if (!"Success".equals(resultMap.get("returnstatus"))) {
                            //发送错误,不再重试
                            sendSmsUpdate.setStatus(2);
                            sendSmsUpdate.setRemk(resultMap.get("message") + "");
                        } else {
                            sendSmsUpdate.setStatus(1);
                            sendSmsUpdate.setMsgId(null);
                            sendSmsUpdate.setRemk(null);

                            //任务成功
                            aiSmsJobDAO.updateSingleJobBySendSmsStatus("3", sendSmsUpdate.getId());

                            //更新每条短信的msgId
                            String resp = ((Map<String, String>) resultMap.get("resplist")).get("resp");
                            String[] respContent = resp.split("#@#");
                            if ("0".equals(respContent[2])) {
                                //成功
                                sendSmsUpdate.setStatus(1);
                                sendSmsUpdate.setMsgId(respContent[0]);
                                sendSmsUpdate.setRetryTimes(null);
                            } else {
                                //失败，不再重试
                                sendSmsUpdate.setStatus(2);
                                sendSmsUpdate.setStatus(0);
                                sendSmsUpdate.setRemk(respContent[2]);
                            }
                        }
                    } catch (Exception e) {
                        log.error("万众联动短信渠道发送异常", e);
                        //发送错误,不再重试
                        sendSmsUpdate.setStatus(2);
                        sendSmsUpdate.setRemk(e.getMessage());
                    } finally {
                        if ("2".equals(sendSmsUpdate.getStatus())) {
                            //发通知
                            Notify errorNotify = Notify.createDispatcherNotify(sendSmsUpdate.getLockId(), "本渠道短信发送出现故障:" + sendSmsUpdate.getRemk());
                            notifyDAO.insert(errorNotify);
                        }
                        sendSmsDAO.updateSendSmsSendResult(sendSmsUpdate);
                    }

                },
                t -> {
                    log.error("短信渠道发送异常", t);

                    sendSms.setStatus(0);
                    sendSms.setRetryTimes(sendSms.getRetryTimes() + 1);
                    sendSms.setRemk(StringUtils.substring(t.getMessage(), 0, 255));
                    //解锁
                    sendSmsDAO.updateAndUnLockSendSms(sendSms);

                    if (sendSms.getMaxRetryTimes() <= sendSms.getRetryTimes()) {
                        //短信任务异常
                        aiSmsJobDAO.updateSingleJobBySendSmsStatus("9", sendSms.getId());
                    }
                });
    }

}
