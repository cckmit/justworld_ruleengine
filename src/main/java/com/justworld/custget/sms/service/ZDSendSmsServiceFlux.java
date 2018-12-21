package com.justworld.custget.sms.service;

import com.justworld.custget.ruleengine.service.bo.Notify;
import com.justworld.custget.ruleengine.service.bo.SendSms;
import com.justworld.custget.ruleengine.service.bo.SmsDispatcher;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 万众联动
 */
@Slf4j
@Service
public class ZDSendSmsServiceFlux extends BaseSendSmsService {

    @Scheduled(cron = "30 0/10 * * * *")
    public void sendDbSms() {
        super.sendDbSms();
    }

    @KafkaListener(topics = "send_sms_notify_4", containerFactory = "kafkaListenerBatchConsumerFactory")
    public void sendQueueSms(List<SendSms> sendSmsList) {
        super.sendQueueSms(sendSmsList);
    }

    @Override
    protected Function<Triple<SmsDispatcher, WebClient, SendSms>, Mono<String>> getRequestBuilder() {
        return (param)->{
            SmsDispatcher dispatcher = param.getLeft();
            SendSms sendSms = param.getRight();
            String timestamp = DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMddHHmmss");
            String sign = null;
            try {
                sign = DigestUtils.md5DigestAsHex((dispatcher.getPassword() + timestamp).getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return param.getMiddle().get()
                    .uri("?uid={account}&pw={password}&mb={phone}&ms={content}&tm={timestamp}&ex={ex}",
                            dispatcher.getAccount(), sign, sendSms.getPhone(), sendSms.getContent(), timestamp, "")
                    .retrieve()
                    .bodyToMono(String.class);
        };
    }

    @Override
    protected Function<String, Triple<Integer, String, String>> handleResult() {
        return sendResult -> {
            String[] rt = sendResult.split(",");
            int status = 1;
            String msgId = null;
            String remk = "";
            if ("0".equals(rt[0])) {
                status = 1;
                msgId = rt[1];
            } else if ("Retry".equals(rt[0])) {
                status = 0;
                remk = StringUtils.substring(rt[1] + "", 0, 255);
            } else {
                status = 2;
                remk = rt.length > 1 ? rt[1] : null;
            }

            return Triple.of(status,msgId,remk);

        };
    }

    public Mono<String> receiveSmsReport(String data) {
        log.debug("receive report data={}", data);
        String[] messages = data.split("[|]");
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
                    default:
                        sendSms.setSendResult("2");
                        break;
                }
                sendSms.setRemk(result[2]);
                sendSms.setDoneTime(new Date());
                sendSmsDAO.updateSendResult(sendSms);
                return Mono.just("0");
            } catch (Exception e) {
                log.error("处理状态报告出错", e);
                return Mono.error(e);
            }
        }).onErrorReturn("9").last();

        return reportMono;
    }

    @Override
    public String getDispatcherId() {
        return "4";
    }
}
