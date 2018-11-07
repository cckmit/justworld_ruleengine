package com.justworld.custget.ruleengine.service.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * sms_dispatcher
 * @author 
 */
@Data public class SmsDispatcher implements Serializable {
    /**
     * 发送渠道代码
     */
    private String dispatcherKey;

    /**
     * 发送渠道名称
     */
    private String name;

    /**
     * 备注
     */
    private String remk;

    /**
     * 状态：1=正常，2=停用
     */
    private String status;

    private static final long serialVersionUID = 1L;

}