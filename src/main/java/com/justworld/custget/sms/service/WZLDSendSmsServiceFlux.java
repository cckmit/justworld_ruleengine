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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 万众联动
 */
@Slf4j
@Service
public class WZLDSendSmsServiceFlux extends BaseSendSmsService {

    private String dispatcherId = "5";

    @Scheduled(cron = "25 0/10 * * * *")
    public void sendDbSms() {
        super.sendDbSms();
    }

    @KafkaListener(topics = "send_sms_notify_5", containerFactory = "kafkaListenerBatchConsumerFactory")
    public void sendQueueSms(List<SendSms> sendSmsList) {
        super.sendQueueSms(sendSmsList);

    }

    public void sendSms(List<SendSms> sendSmsList) {

            //查询短信发送的用户名密码
            String sendUrl = baseConfigDAO.selectByPrimaryKey("WANZONGLIANDONG_SMS_CONFIG", "SEND_URL").getCfgValue();
            String account = baseConfigDAO.selectByPrimaryKey("WANZONGLIANDONG_SMS_CONFIG", "ACCOUNT").getCfgValue();
            String password = baseConfigDAO.selectByPrimaryKey("WANZONGLIANDONG_SMS_CONFIG", "PASSWORD").getCfgValue();
            String extno = baseConfigDAO.selectByPrimaryKey("WANZONGLIANDONG_SMS_CONFIG", "EXTNO").getCfgValue();

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
                map.add("action", "send");
                map.add("account", account);
                map.add("password", password);
                map.add("extno", extno);

                map.add("mobile", sendSms.getPhone());
                map.add("content", sendSms.getContent());

                Mono<String> r = webClient.post()
                        .syncBody(map)
                        .retrieve()
                        .bodyToMono(String.class);

                r.onErrorResume(e -> {
                    log.error("万众联动短信渠道发送异常", e);
                    //自行组装报文重试
                    return Mono.just("<?xml version=\"1.0\" encoding=\"utf-8\" ?>" +
                            "<returnsms>\n" +
                            "<returnstatus>Retry</returnstatus>" +
                            "<message>" + e.getMessage() + "</message> " +
                            "</returnsms>\n");
                });

                return Mono.zip(r, Mono.just(sendSms)).flatMap(p -> {
                    SendSms sms = p.getT2();
                    String sendResult = p.getT1();
                    long cost = System.currentTimeMillis()-start;
                    log.debug("短信ID[{}]发送耗时：{}",sms.getId(),cost);
                    try {
                        log.trace("response xml = {}", sendResult);
                        Map resultMap = new XmlMapper().readValue(sendResult, Map.class);
                        sms.setSendTime(new Date());
                        if ("Success".equals(resultMap.get("returnstatus"))) {

                            //更新每条短信的msgId
                            String resp = ((Map<String, String>) resultMap.get("resplist")).get("resp");
                            String[] respContent = resp.split("#@#");
                            if ("0".equals(respContent[2])) {
                                //成功
                                log.debug("万众短信发送成功");
                                sms.setStatus(1);
                                sms.setMsgId(respContent[0]);
                                sms.setRetryTimes(null);
                                sms.setRemk(null);
                                sms.setCost(Math.toIntExact(cost));
                            } else {
                                //失败，不再重试
                                log.debug("万众短信发送失败:{}", respContent[2]);
                                sms.setStatus(2);
                                sms.setMsgId(null);
                                sms.setRetryTimes(null);
                                sms.setRemk(respContent[2]);
                            }

                        } else if ("Retry".equals(resultMap.get("returnstatus"))) {
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
                            sms.setRemk(resultMap.get("message") + "");
                        }
                    } catch (IOException e) {
                        log.error("万众联动短信渠道返回结果异常", e);
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

    @Scheduled(cron = "0/5 * * * * *")
    public void receiveSmsReport() {
        //查询待报告任务
        int reportCount = sendSmsDAO.countSmsForReport(dispatcherId);
        if (reportCount == 0) {
            return;
        }
        log.debug("待返回报告的短信数量:{}", reportCount);

        String sendUrl = baseConfigDAO.selectByPrimaryKey("WANZONGLIANDONG_SMS_CONFIG", "SEND_URL").getCfgValue();
        String account = baseConfigDAO.selectByPrimaryKey("WANZONGLIANDONG_SMS_CONFIG", "ACCOUNT").getCfgValue();
        String password = baseConfigDAO.selectByPrimaryKey("WANZONGLIANDONG_SMS_CONFIG", "PASSWORD").getCfgValue();

        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(options -> options.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000).compression(true).afterNettyContextInit(ctx -> {
            ctx.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
        }));

        WebClient webClient = WebClient.builder()
                .clientConnector(connector)
                .baseUrl(sendUrl)
                .build();

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("action", "report");
        map.add("account", account);
        map.add("password", password);

        Mono<String> r = webClient.post()
                .syncBody(map)
                .retrieve()
                .bodyToMono(String.class);

        r.subscribe(resultXml -> {
            log.debug("查询状态报告结果:{}", resultXml);
            try {
                Document document = DocumentHelper.parseText(resultXml);
                List<Element> statusboxList = document.getRootElement().elements("statusbox");
                for (Element node : statusboxList) {
                    SendSms sendSms = new SendSms();
                    sendSms.setMsgId(node.elementText("taskid"));
                    sendSms.setSendResult(node.elementText("status").equals("10") ? "1" : "2");
                    sendSms.setPhone(node.elementText("mobile"));
                    if ("1".equals(sendSms.getSendResult())) {
                        sendSms.setDoneTime(DateUtils.parseDate(node.elementText("errorcode"), "yyyy-MM-dd HH:mm:ss"));
                    } else {
                        sendSms.setDoneTime(new Date());
                    }
                    sendSms.setRemk(node.elementText("errorcode"));
                    sendSmsDAO.updateSendResult(sendSms);
                }
            } catch (Exception e) {
                log.error("查询状态报告出错", e);
            }
        });

    }

    @Override
    public String getDispatcherId() {
        return "5";
    }
}
