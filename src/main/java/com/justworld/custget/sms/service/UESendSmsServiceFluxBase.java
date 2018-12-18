package com.justworld.custget.sms.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.justworld.custget.ruleengine.service.bo.SendSms;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 万众联动
 */
@Slf4j
public abstract class UESendSmsServiceFluxBase extends BaseSendSmsService {

    abstract protected String getSendUrl();
    abstract protected String getAccount();
    abstract protected String getPassword();

    public void sendSms(List<SendSms> sendSmsList) {

        //查询短信发送的用户名密码
        String sendUrl = getSendUrl();
        String account = getAccount();
        String password = getPassword();

        Flux<SendSms> sendSmsFlux = Flux.fromIterable(sendSmsList);
        log.debug("本次短信发送条数:{}", sendSmsList.size());

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
            map.add("username", account);
            map.add("userpwd", password);

            map.add("mobiles", sendSms.getPhone());
            try {
                map.add("content", sendSms.getContent());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            Mono<String> r = webClient.post()
                    .syncBody(map)
                    .retrieve()
                    .bodyToMono(String.class);

            r.onErrorResume(e -> {
                log.error("UE短信渠道发送异常", e);
                //自行组装报文重试
                return Mono.just("<?xml version=\"1.0\" encoding=\"utf-8\" ?>" +
                        "<sendresult>\n" +
                        "<errorcode>Retry</errorcode>" +
                        "<message>" + e.getMessage() + "</message>" +
                        "</sendresult>\n");
            });

            return Mono.zip(r, Mono.just(sendSms)).flatMap(p -> {
                SendSms sms = p.getT2();
                String sendResult = p.getT1();
                long cost = System.currentTimeMillis() - start;
                log.debug("短信ID[{}]发送耗时：{}", sms.getId(), cost);
                try {
                    log.trace("response xml = {}", sendResult);
                    Map resultMap = new XmlMapper().readValue(sendResult, Map.class);
                    sms.setSendTime(new Date());
                    if ("1".equals(resultMap.get("errorcode"))) {
                        //成功
                        log.debug("UE短信发送成功");
                        sms.setStatus(1);
                        sms.setMsgId(resultMap.get("SMSID") + "");
                        sms.setRetryTimes(null);
                        sms.setRemk(null);
                        sms.setCost(Math.toIntExact(cost));

                    } else if ("Retry".equals(resultMap.get("errorcode"))) {
                        log.debug("请求异常，重试");
                        sms.setStatus(0);
                        sms.setRetryTimes(sms.getRetryTimes() + 1);
                        sms.setRemk(StringUtils.substring(resultMap.get("message") + "", 0, 255));
                    } else {
                        //发送失败
                        log.info("发送失败:{}", sendResult);
                        sms.setStatus(2);
                        sms.setMsgId(null);
                        sms.setRetryTimes(null);
                        sms.setRemk(resultMap.get("errorcode") + "");
                    }
                } catch (IOException e) {
                    log.error("UE短信渠道返回结果异常", e);
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
        log.debug("短信数量{},总处理耗时{}", totalSend.size(), System.currentTimeMillis() - start);
    }


    public Mono<String> receiveSms(String pushtype, String data) {
        log.debug("receive pushtype={},data={}", pushtype, data);
        if ("report".equals(pushtype)) {  //状态报告
            log.debug("receive sms report");
            String[] messages = data.split(";");
            Mono<String> reportMono = Flux.fromArray(messages).flatMap(message -> {
                try {

                    String[] result = message.split(",");
                    SendSms sendSms = new SendSms();
                    sendSms.setMsgId(result[0]);
                    sendSms.setPhone(result[1]);
                    switch (result[2]) {
                        case "DELIVRD":
                            sendSms.setSendResult("1");
                            break;
                        case "UNDELIV":
                        case "BLACKLIST":
                        case "UNKNOW":
                        default:
                            sendSms.setSendResult("2");
                            break;
                    }
                    sendSms.setRemk(result[2]);
                    sendSms.setDoneTime(DateUtils.parseDate(result[3], "yyyy-M-dd HH:mm:ss"));
                    sendSmsDAO.updateSendResult(sendSms);
                    return Mono.just("0");
                } catch (Exception e) {
                    log.error("处理状态报告出错", e);
                    return Mono.error(e);
                }
            }).onErrorReturn("9").last();

            return reportMono;
        }
        return Mono.just("0");
    }

    @Override
    public String getDispatcherId() {
        return "6";
    }
}
