package com.justworld.custget.sms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.justworld.custget.ruleengine.dao.SmsDispatcherDAO;
import com.justworld.custget.ruleengine.service.bo.SendSms;
import com.justworld.custget.ruleengine.service.bo.SmsDispatcher;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Triple;
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
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 万众联动
 */
@Slf4j
@Service
public class WZLDSendSmsServiceFlux extends BaseSendSmsService {
    @Autowired
    private ObjectMapper objectMapper;

    @Scheduled(cron = "25 0/10 * * * *")
    public void sendDbSms() {
        super.sendDbSms();
    }

    @KafkaListener(topics = "send_sms_notify_5", containerFactory = "kafkaListenerBatchConsumerFactory")
    public void sendQueueSms(List<SendSms> sendSmsList) {
        super.sendQueueSms(sendSmsList);

    }

    @Override
    protected Function<Triple<SmsDispatcher, WebClient, SendSms>, Mono<String>> getRequestBuilder() {
        return (param) -> {
            SmsDispatcher dispatcher = param.getLeft();
            SendSms sendSms = param.getRight();
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("action", "send");
            map.add("account", dispatcher.getDispatcherKey());
            map.add("password", dispatcher.getPassword());
            try {
                String extno = (String) objectMapper.readValue(dispatcher.getExtraParam(),Map.class).get("EXTNO");
                map.add("extno", extno);
            } catch (IOException e) {
                e.printStackTrace();
            }

            map.add("mobile", sendSms.getPhone());
            map.add("content", sendSms.getContent());

            return param.getMiddle().post()
                    .syncBody(map)
                    .retrieve()
                    .bodyToMono(String.class);
        };
    }

    @Override
    protected Function<String, Triple<Integer, String, String>> handleResult() {
        return sendResult -> {
            log.trace("response xml = {}", sendResult);
            int status = 1;
            String msgId = null;
            String remk = "";
            try {
                Map resultMap = new XmlMapper().readValue(sendResult, Map.class);
                if ("Success".equals(resultMap.get("returnstatus"))) {
                    String resp = ((Map<String, String>) resultMap.get("resplist")).get("resp");
                    String[] respContent = resp.split("#@#");
                    status = 1;
                    msgId = respContent[0];
                } else if ("Retry".equals(resultMap.get("returnstatus"))) {
                    status = 0;
                    remk = StringUtils.substring(resultMap.get("message") + "", 0, 255);
                } else {
                    status = 2;
                    remk = resultMap.get("returnstatus")+"_"+resultMap.get("message") + "";
                }
            } catch (Exception e) {
                status = 2;
                remk = StringUtils.substring(e.getMessage(), 0, 255);
            }
            return Triple.of(status, msgId, remk);

        };
    }

    @Scheduled(cron = "0/5 * * * * *")
    public void receiveSmsReport() {
        //查询待报告任务
        int reportCount = sendSmsDAO.countSmsForReport(getDispatcherId());
        if (reportCount == 0) {
            return;
        }
        log.debug("待返回报告的短信数量:{}", reportCount);

        SmsDispatcher dispatcher = smsDispatcherDAO.selectByPrimaryKey(getDispatcherId());

        String sendUrl = dispatcher.getSendUrl();
        String account = dispatcher.getAccount();
        String password = dispatcher.getPassword();

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
