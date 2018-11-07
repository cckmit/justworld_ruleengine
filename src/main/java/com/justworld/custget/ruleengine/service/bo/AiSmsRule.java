package com.justworld.custget.ruleengine.service.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * ai_sms_rule
 * @author 
 */
@Data
public class AiSmsRule implements Serializable {
    /**
     * 规则编号
     */
    private Integer id;

    /**
     * 规则类型
     */
    private String ruleType;

    /**
     * 规则关键字
     */
    private String ruleKey;

    /**
     * 短信发送渠道ID
     */
    private String dispatcherKey;

    /**
     * 规则状态：1=休眠，2=激活
     */
    private String status;

    private static final long serialVersionUID = 1L;

}