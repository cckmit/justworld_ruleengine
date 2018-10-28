package com.justworld.custget.ruleengine.service.smsdispatcher;

import com.justworld.custget.ruleengine.service.bo.AiSmsJob;
import com.justworld.custget.ruleengine.service.bo.SmsDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI短信发送渠道选择器
 */
@Slf4j
@Service
public class AiSmsDispatcherSelector {

    /**
     * 选择挂机任务适用的短信渠道 TODO
     * @param aiSmsJob
     * @return
     */
    public SmsDispatcher select(AiSmsJob aiSmsJob){
        return null;
    }
}
