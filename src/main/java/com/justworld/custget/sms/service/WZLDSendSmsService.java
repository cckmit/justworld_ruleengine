package com.justworld.custget.sms.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.justworld.custget.ruleengine.dao.AiSmsJobDAO;
import com.justworld.custget.ruleengine.dao.BaseConfigDAO;
import com.justworld.custget.ruleengine.dao.NotifyDAO;
import com.justworld.custget.ruleengine.dao.SendSmsDAO;
import com.justworld.custget.ruleengine.service.bo.Notify;
import com.justworld.custget.ruleengine.service.bo.SendSms;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 万众联动
 */
@Slf4j
@Service
public class WZLDSendSmsService {

    @Autowired
    private SendSmsDAO sendSmsDAO;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private BaseConfigDAO baseConfigDAO;
    @Autowired
    private NotifyDAO notifyDAO;
    @Autowired
    private AiSmsJobDAO aiSmsJobDAO;

    @Scheduled(cron = "10 0/30 * * * *")
    @KafkaListener(topics = "send_sms_notify_5")
    @Transactional
    public void sendSms() {
        String dispatcherId = "5";
        //锁定任务
        String lockId = UUID.randomUUID().toString();
        int lockCount = sendSmsDAO.lockSendSms(dispatcherId,lockId,200);
        if(lockCount>0){
            log.trace("本次万众联动短信渠道批量发送任务共{}条",lockCount);
        }else{
            return;
        }

        SendSms sendSmsUpdate = new SendSms();
        sendSmsUpdate.setDispatcherId(dispatcherId);
        sendSmsUpdate.setLockId(lockId);
        try {
            List<SendSms> sendSmsList = sendSmsDAO.queryLockedSendSmsList(dispatcherId, lockId);

            String content = URLEncoder.encode(sendSmsList.stream().map(s -> s.getPhone() + "\t" + s.getContent()).collect(Collectors.joining("\n")), "UTF-8");

            //查询短信发送的用户名密码
            String sendUrl = baseConfigDAO.selectByPrimaryKey("WANZONGLIANDONG_SMS_CONFIG", "SEND_URL").getCfgValue();
            String account = baseConfigDAO.selectByPrimaryKey("WANZONGLIANDONG_SMS_CONFIG", "ACCOUNT").getCfgValue();
            String password = baseConfigDAO.selectByPrimaryKey("WANZONGLIANDONG_SMS_CONFIG", "PASSWORD").getCfgValue();
            String extno = baseConfigDAO.selectByPrimaryKey("WANZONGLIANDONG_SMS_CONFIG", "EXTNO").getCfgValue();

            for (SendSms sendSms : sendSmsList) {

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                map.add("action", "send");
                map.add("account", account);
                map.add("password", password);
                map.add("mobile", sendSms.getPhone());
                map.add("content", sendSms.getContent());
                map.add("extno", extno);
                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

                String returnXml = restTemplate.postForObject(sendUrl, request, String.class);
                log.info("万众联动渠道短信发送返回码{}", returnXml);
                Map resultMap = new XmlMapper().readValue(returnXml, Map.class);

                try {
                    if (!"Success".equals(resultMap.get("returnstatus"))) {
                        //发送错误
                        sendSmsUpdate.setStatus(2);
                        sendSmsUpdate.setRemk(resultMap.get("message") + "");
                        //发通知
                        Notify errorNotify = Notify.createDispatcherNotify(lockId, "本渠道短信发送出现故障:" + resultMap.get("returnstatus"));
                        notifyDAO.insert(errorNotify);

                        //短信任务异常
                        aiSmsJobDAO.updateJobBySendSmsStatus("9", dispatcherId, lockId);
                    } else {
                        sendSmsUpdate.setStatus(1);
                        sendSmsUpdate.setMsgId(null);
                        sendSmsUpdate.setRemk(null);

                        //任务成功
                        aiSmsJobDAO.updateJobBySendSmsStatus("3", dispatcherId, lockId);

                        //更新每条短信的msgId
                        String resp = ((Map<String, String>) resultMap.get("resplist")).get("resp");
                        String[] respContent = resp.split("#@#");
                        SendSms updateSms = new SendSms();
                        updateSms.setDispatcherId(dispatcherId);
                        updateSms.setLockId(lockId);
                        updateSms.setPhone(respContent[1]);
                        if ("0".equals(respContent[2])) {
                            //成功
                            updateSms.setStatus(1);
                            updateSms.setMsgId(respContent[0]);
                        } else {
                            updateSms.setRetryTimes(1);
                            updateSms.setRemk(respContent[2]);
                        }
                        sendSmsDAO.updateSendSmsSendResult(updateSms);
                    }
                } catch (Exception e){

                }
            }

        } catch (Exception e){
            log.error("仲达短信渠道发送异常",e);
            sendSmsUpdate.setStatus(0);
            sendSmsUpdate.setRetryTimes(1);
            sendSmsUpdate.setRemk(e.getMessage());

        } finally {
          //解锁
            sendSmsDAO.updateAndUnLockSendSms(sendSmsUpdate);
        }
    }

}
