package com.justworld.custget.sms.service;

import com.justworld.custget.ruleengine.dao.SendSmsDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SendSmsService {

    @Autowired
    private SendSmsDAO sendSmsDAO;

    @Scheduled(cron = "40 0/30 * * * *")
    public void unlockOvertimeRecord(){
        sendSmsDAO.unlockOvertimeRecord();
    }
}
