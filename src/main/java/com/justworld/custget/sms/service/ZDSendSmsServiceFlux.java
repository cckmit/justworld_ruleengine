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
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
public class ZDSendSmsServiceFlux extends BaseSendSmsService {

    @Scheduled(cron = "10 0/1 * * * *")
    public void sendDbSms() {
        super.sendDbSms();
    }

    @KafkaListener(topics = "send_sms_notify_4", containerFactory = "kafkaListenerBatchConsumerFactory")
    public void sendQueueSms(List<SendSms> sendSmsList) {
        super.sendQueueSms(sendSmsList);
    }

    @Override
    protected void sendSms(List<SendSms> sendSmsList) {

        Flux<SendSms> sendSmsFlux = Flux.fromIterable(sendSmsList);
        log.debug("本次短信发送条数:{}", sendSmsList.size());

        //查询短信发送的用户名密码
        String sendUrl = baseConfigDAO.selectByPrimaryKey("ZHONGDA_SMS_CONFIG", "SEND_URL").getCfgValue();
        String account = baseConfigDAO.selectByPrimaryKey("ZHONGDA_SMS_CONFIG", "ACCOUNT").getCfgValue();
        String password = baseConfigDAO.selectByPrimaryKey("ZHONGDA_SMS_CONFIG", "PASSWORD").getCfgValue();
        String timestamp = DateFormatUtils.format(System.currentTimeMillis(),"yyyyMMddHHmmss");
        String sign = DigestUtils.md5DigestAsHex((password+timestamp).getBytes());

        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(options -> options.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000).compression(true).afterNettyContextInit(ctx -> {
            ctx.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
        }));

        WebClient webClient = WebClient.builder()
                .clientConnector(connector)
                .baseUrl(sendUrl)
                .build();

        long start = System.currentTimeMillis();
        Flux<SendSms> sendSmsOverFlux = sendSmsFlux.flatMap(sendSms -> {
            log.debug("本次发送短信ID[{}]", sendSms.getId());
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("ua", account);
            map.add("pw", sign);
            map.add("mb", sendSms.getPhone());
            map.add("tm", timestamp);
            try {
                map.add("ms", URLEncoder.encode(sendSms.getContent(),"utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            map.add("ex", "");
            map.add("dm", "");

            Mono<String> r = webClient.post()
                    .syncBody(map)
                    .retrieve()
                    .bodyToMono(String.class);

            r.onErrorResume(e -> {
                log.error("厦门仲达短信渠道发送异常", e);
                //自行组装报文重试
                return Mono.just("Retry,"+e.getMessage());
            });

            return Mono.zip(r, Mono.just(sendSms)).flatMap(p -> {
                SendSms sms = p.getT2();
                String sendResult = p.getT1();
                log.debug("短信ID[{}]发送耗时：{}",sms.getId(),System.currentTimeMillis()-start);
                try {
                    log.trace("response = {}", sendResult);
                    String[] rt = sendResult.split(",");
                    sms.setSendTime(new Date());
                    if ("0".equals(rt[0])) {
                        //成功
                        log.debug("仲达短信发送成功");
                        sms.setStatus(1);
                        sms.setMsgId(rt[1]);
                        sms.setRetryTimes(null);
                        sms.setRemk(null);

                    } else if ("Retry".equals(rt[0])) {
                        log.debug("请求异常，重试");
                        sms.setStatus(0);
                        sms.setRetryTimes(sms.getRetryTimes() + 1);
                        sms.setRemk(StringUtils.substring(rt[1] + "", 0, 255));
                    } else {
                        //发送失败
                        log.info("发送失败:{}", sendResult);
                        sms.setStatus(2);
                        sms.setMsgId(null);
                        sms.setRetryTimes(null);
                        sms.setRemk(rt[1]);
                    }
                } catch (Exception e) {
                    log.error("厦门仲达短信渠道返回结果异常", e);
                    sms.setStatus(2);
                    sms.setMsgId(null);
                    sms.setRetryTimes(null);
                    sms.setRemk(StringUtils.substring(sendResult, 0, 255));
                } finally {
                    sendSmsDAO.updateSendSmsSendResult(sms);
                    if ((sms.getRetryTimes() != null && sms.getMaxRetryTimes() <= sms.getRetryTimes()) || sms.getStatus().equals("2")) {
                        //短信任务异常
                        aiSmsJobDAO.updateSingleJobBySendSmsStatus("9", sendSms.getId());
                    } else {
                        //任务成功
                        aiSmsJobDAO.updateSingleJobBySendSmsStatus("3", sms.getId());
                    }
                }
                return Mono.just(sms);
            });
        });

        List<SendSms> totalSend = sendSmsOverFlux.collectList().block();
        log.debug("短信数量{},总处理耗时{}", totalSend.size(), System.currentTimeMillis()-start);
    }

    @Override
    public String getDispatcherId() {
        return "4";
    }
}
