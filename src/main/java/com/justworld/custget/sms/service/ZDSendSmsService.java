package com.justworld.custget.sms.service;

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
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ZDSendSmsService {

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
    @KafkaListener(topics = "send_sms_notify_4")
    private void sendJuDaSms() {
        String dispatcherId = "4";
        //锁定任务
        String lockId = UUID.randomUUID().toString();
        int lockCount = sendSmsDAO.lockSendSms(dispatcherId,lockId,200);
        if(lockCount>0){
            log.trace("本次聚得短信渠道批量发送任务共{}条",lockCount);
        }else{
            return;
        }

        SendSms sendSmsUpdate = new SendSms();
        sendSmsUpdate.setDispatcherId(dispatcherId);
        sendSmsUpdate.setLockId(lockId);
        try {
            List<SendSms> sendSmsList = sendSmsDAO.queryLockedSendSmsList(dispatcherId, lockId);

            //查询短信发送的用户名密码
            String sendUrl = baseConfigDAO.selectByPrimaryKey("ZHONGDA_SMS_CONFIG", "SEND_URL").getCfgValue();
            String account = baseConfigDAO.selectByPrimaryKey("ZHONGDA_SMS_CONFIG", "ACCOUNT").getCfgValue();
            String password = baseConfigDAO.selectByPrimaryKey("ZHONGDA_SMS_CONFIG", "PASSWORD").getCfgValue();

            String timestamp = DateFormatUtils.format(System.currentTimeMillis(),"yyyyMMddHHmmss");
            String sign = DigestUtils.md5DigestAsHex((password+timestamp).getBytes());
            for (SendSms sendSms : sendSmsList) {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("uid", account);
            map.add("pw", sign);
            map.add("mb", sendSms.getPhone());
            map.add("tm", timestamp);
            map.add("ms", sendSms.getContent());
            map.add("ex", "01");
            map.add("dm", "");
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            String rtcd = restTemplate.postForObject(sendUrl, request, String.class);
            log.info("仲达渠道短信发送返回码{}", rtcd);

            String[] rt = rtcd.split(",");
            if(!"0".equals(rt[0])){
                //发送错误
                sendSmsUpdate.setStatus(2);
                sendSmsUpdate.setRemk(rtcd);
                //发通知
                Notify errorNotify = Notify.createDispatcherNotify(lockId,"本渠道短信发送出现故障:"+rt[0]);
                notifyDAO.insert(errorNotify);

                //短信任务异常
                aiSmsJobDAO.updateJobBySendSmsStatus("9",dispatcherId,lockId);
            }else{
                sendSmsUpdate.setStatus(1);
                sendSmsUpdate.setMsgId(rt[1]);

                //任务成功
                aiSmsJobDAO.updateJobBySendSmsStatus("3",dispatcherId,lockId);
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
