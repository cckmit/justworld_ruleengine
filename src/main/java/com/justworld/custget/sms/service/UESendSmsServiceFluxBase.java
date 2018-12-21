package com.justworld.custget.sms.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.justworld.custget.ruleengine.service.bo.SendSms;
import com.justworld.custget.ruleengine.service.bo.SmsDispatcher;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
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
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 校验通
 */
@Slf4j
public abstract class UESendSmsServiceFluxBase extends BaseSendSmsService {

    @Override
    protected Function<Triple<SmsDispatcher, WebClient, SendSms>, Mono<String>> getRequestBuilder() {
        return (param)->{
            SmsDispatcher dispatcher = param.getLeft();
            SendSms sendSms = param.getRight();
            WebClient webClient = param.getMiddle();
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("username", dispatcher.getAccount());
            map.add("userpwd", dispatcher.getPassword());

            map.add("mobiles", sendSms.getPhone());
            try {
                map.add("content", sendSms.getContent());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return webClient.post()
                    .syncBody(map)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(e -> {
                //自行组装报文重试
                return Mono.just("<?xml version=\"1.0\" encoding=\"utf-8\" ?>" +
                        "<sendresult>\n" +
                        "<errorcode>Retry</errorcode>" +
                        "<message>" + e.getMessage() + "</message>" +
                        "</sendresult>\n");
            });
        };
    }

    @Override
    protected Function<String, Triple<Integer, String, String>> handleResult() {
        return sendResult -> {
            String[] rt = sendResult.split(",");
            int status = 1;
            String msgId = null;
            String remk = "";
            try{
                Map resultMap = new XmlMapper().readValue(sendResult, Map.class);
                if ("1".equals(resultMap.get("errorcode"))) {
                    status = 1;
                    msgId = resultMap.get("SMSID") + "";
                } else if ("Retry".equals(resultMap.get("errorcode"))) {
                    status = 0;
                    remk = StringUtils.substring(resultMap.get("message") + "", 0, 255);
                } else {
                    status = 2;
                    remk = resultMap.get("errorcode") + "";
                }
            } catch (Exception e){
                status = 2;
                remk = StringUtils.substring(e.getMessage(), 0, 255);
            }

            return Triple.of(status,msgId,remk);

        };
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
    abstract public String getDispatcherId();
}
