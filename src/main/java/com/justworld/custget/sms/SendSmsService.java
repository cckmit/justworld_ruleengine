package com.justworld.custget.sms;

import com.justworld.custget.ruleengine.dao.BaseConfigDAO;
import com.justworld.custget.ruleengine.dao.NotifyDAO;
import com.justworld.custget.ruleengine.dao.SendSmsDAO;
import com.justworld.custget.ruleengine.dao.SmsDispatcherDAO;
import com.justworld.custget.ruleengine.service.bo.Notify;
import com.justworld.custget.ruleengine.service.bo.SendSms;
import com.justworld.custget.ruleengine.service.bo.SmsDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class SendSmsService {

    @Autowired
    private SendSmsDAO sendSmsDAO;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private BaseConfigDAO baseConfigDAO;
    @Autowired
    private NotifyDAO notifyDAO;

    /**
     * 用聚达渠道发送数据库中的待发短信
     */
    @Scheduled(cron = "0 0/10 * * * *")
    @KafkaListener(topics = "send_sms_notify_1")
    public void sendJuDaSmsYd(){
        String dispatcherId = "1";
        String username = baseConfigDAO.selectByPrimaryKey("JUDA_SMS_CONFIG", "USERNAME_YD").getCfgValue();
        String password = baseConfigDAO.selectByPrimaryKey("JUDA_SMS_CONFIG", "PASSWORD_YD").getCfgValue();
        sendJuDaSms(dispatcherId, username, password);


    }

    /**
     * 用聚达电信渠道发送数据库中的待发短信
     */
    @Scheduled(cron = "10 0/10 * * * *")
    @KafkaListener(topics = "send_sms_notify_3")
    public void sendJuDaSmsDX(){
        String dispatcherId = "3";
        String username = baseConfigDAO.selectByPrimaryKey("JUDA_SMS_CONFIG", "USERNAME_DX").getCfgValue();
        String password = baseConfigDAO.selectByPrimaryKey("JUDA_SMS_CONFIG", "PASSWORD_DX").getCfgValue();
        sendJuDaSms(dispatcherId, username, password);


    }

    /**
     * 用聚达联通渠道发送数据库中的待发短信
     */
    @Scheduled(cron = "20 0/10 * * * *")
    @KafkaListener(topics = "send_sms_notify_2")
    public void sendJuDaSmsLT(){
        String dispatcherId = "2";
        String username = baseConfigDAO.selectByPrimaryKey("JUDA_SMS_CONFIG", "USERNAME_LT").getCfgValue();
        String password = baseConfigDAO.selectByPrimaryKey("JUDA_SMS_CONFIG", "PASSWORD_LT").getCfgValue();
        sendJuDaSms(dispatcherId, username, password);


    }

    private void sendJuDaSms(String dispatcherId, String username, String password) {
        //锁定任务
        String lockId = UUID.randomUUID().toString();
        int lockCount = sendSmsDAO.lockSendSms(dispatcherId,lockId,100);
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
            String sendUrl = baseConfigDAO.selectByPrimaryKey("JUDA_SMS_CONFIG", "SEND_URL").getCfgValue();
            String encode = baseConfigDAO.selectByPrimaryKey("JUDA_SMS_CONFIG", "ENCODE").getCfgValue();
            String encodeType = baseConfigDAO.selectByPrimaryKey("JUDA_SMS_CONFIG", "ENCODE_TYPE").getCfgValue();

            StringBuilder mobile = new StringBuilder();
            StringBuilder content = new StringBuilder();
            for (SendSms sendSms : sendSmsList) {
                mobile.append(sendSms.getPhone()).append(",");
                content.append(sendSms.getContent()).append(",");
            }
            mobile.deleteCharAt(mobile.length() - 1);
            content.deleteCharAt(content.length() - 1);

            String sendContent = new String(content.toString().getBytes(),encode);
            if("base64".equals(encodeType)){
                sendContent = Base64Utils.encodeToString(sendContent.getBytes(encode));
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("uid", username);
            map.add("password", DigestUtils.md5DigestAsHex(password.getBytes()).toUpperCase());
            map.add("mobile", mobile.toString());
            map.add("encode", encode);
            map.add("content", sendContent);
            map.add("encodeType", encodeType);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            String cid = restTemplate.postForObject(sendUrl, request, String.class);
            log.info("聚达渠道短信发送返回码{}", cid);

            if(cid.length()<=3){
                //发送错误
                sendSmsUpdate.setStatus(2);
                sendSmsUpdate.setRemk(cid);
                //发通知
                Notify errorNotify = Notify.createDispatcherNotify(lockId,"本渠道短信发送出现故障:"+cid);
                notifyDAO.insert(errorNotify);
            }else{
                sendSmsUpdate.setStatus(1);
                sendSmsUpdate.setMsgId(cid);
            }

        } catch (Exception e){
            log.error("聚达短信渠道发送异常",e);
            sendSmsUpdate.setStatus(0);
            sendSmsUpdate.setRetryTimes(1);
            sendSmsUpdate.setRemk(e.getMessage());

        } finally {
          //解锁
            sendSmsDAO.updateAndUnLockSendSms(sendSmsUpdate);
        }
    }

}
