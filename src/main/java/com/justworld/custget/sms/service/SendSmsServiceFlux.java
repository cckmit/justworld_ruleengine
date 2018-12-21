package com.justworld.custget.sms.service;

import com.justworld.custget.ruleengine.dao.AiSmsJobDAO;
import com.justworld.custget.ruleengine.dao.BaseConfigDAO;
import com.justworld.custget.ruleengine.dao.NotifyDAO;
import com.justworld.custget.ruleengine.dao.SendSmsDAO;
import com.justworld.custget.ruleengine.service.bo.Notify;
import com.justworld.custget.ruleengine.service.bo.SendSms;
import com.justworld.custget.ruleengine.service.bo.SmsDispatcher;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 发送短信流式服务
 */
@Slf4j
@Service
public class SendSmsServiceFlux {

    @Autowired
    private SendSmsDAO sendSmsDAO;
    @Autowired
    private NotifyDAO notifyDAO;
    @Autowired
    private AiSmsJobDAO aiSmsJobDAO;

    public void sendSms(SmsDispatcher dispatcher, Flux<SendSms> sendSmsFlux,
                        Function<Triple<SmsDispatcher, WebClient, SendSms>, Mono<String>> requestSms,
                        Function<String, Triple<Integer,String,String>> getResult) {

        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(options -> options.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000).compression(true).afterNettyContextInit(ctx -> {
            ctx.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
        }));

        WebClient webClient = WebClient.builder()
                .clientConnector(connector)
                .baseUrl(dispatcher.getSendUrl())
                .build();

        sendSmsFlux.flatMap(sendSms -> {
            long start = System.currentTimeMillis();
            log.debug("本次发送短信ID[{}]", sendSms.getId());

            Mono<String> r = requestSms.apply(Triple.of(dispatcher, webClient, sendSms));

            return r.flatMap(reqResult -> {
                int cost = Math.toIntExact(System.currentTimeMillis() - start);
                log.debug("短信ID[{}]发送耗时：{}\n 返回结果:{}", sendSms.getId(), cost,
                        reqResult);
                Triple<Integer,String,String> result = getResult.apply(reqResult);
                sendSms.setSendTime(new Date());
                sendSms.setStatus(result.getLeft());
                sendSms.setMsgId(result.getMiddle());
                sendSms.setRemk(result.getRight());
                sendSms.setCost(cost);
                switch (result.getLeft()){
                    case 1:
                        //成功
                        log.debug(dispatcher.getName()+"短信发送成功");
                        sendSms.setRetryTimes(null);
                        break;
                    case 0:
                        log.debug("请求异常，重试");
                        sendSms.setRetryTimes(sendSms.getRetryTimes() + 1);
                        break;
                    default:
                        //发送失败
                        log.info("发送失败:{}", reqResult);
                        sendSms.setRetryTimes(null);
                }
                return Mono.just(sendSms);
            })
                    .onErrorResume(e -> {
                        log.error(dispatcher.getName() + "短信请求异常", e);
                        sendSms.setStatus(9);   //30分钟后自动解锁重试
                        sendSms.setMsgId(null);
                        sendSms.setRetryTimes(1);
                        sendSms.setRemk(StringUtils.substring(e.getMessage(), 0, 255));
                        return Mono.just(sendSms);

                    });
        }).subscribe(sms -> {
            sendSmsDAO.updateSendSmsSendResult(sms);
            if ((sms.getRetryTimes() != null && sms.getMaxRetryTimes() <= sms.getRetryTimes()) || sms.getStatus() == 2) {
                //短信任务异常
                aiSmsJobDAO.updateSingleJobBySendSmsStatus("9", sms.getId());
                log.error("短信任务失败");
                //发通知
                Notify errorNotify = Notify.createDispatcherNotify(sms.getLockId(), "本渠道短信发送出现故障:" + sms.getRemk());
                notifyDAO.insert(errorNotify);
            } else {
                //任务成功
                aiSmsJobDAO.updateSingleJobBySendSmsStatus("3", sms.getId());
            }
        });
    }

}
