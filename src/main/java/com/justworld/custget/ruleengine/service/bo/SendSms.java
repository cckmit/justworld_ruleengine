package com.justworld.custget.ruleengine.service.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * send_sms
 * @author 
 */
@Data
public class SendSms implements Serializable {
    /**
     * 短信主键，标识唯一
     */
    private Integer id;

    /**
     * 被叫号码
     */
    private String phone;

    /**
     * 短信内容
     */
    private String content;

    /**
     * 短信创建时间
     */
    private Date createTime;

    /**
     * 发送时间
     */
    private Date sendTime;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 客户收到时间
     */
    private Date doneTime;

    /**
     * 移动网关返回的短信ID
     */
    private String msgId;

    /**
     * 短信发送时间分类，用于定义该短信的发送时间
     */
    private String smsType;

    /**
     * 短信发送渠道ID
     */
    private String dispatcherId;

    /**
     * 重试次数
     */
    private Integer retryTimes;

    /**
     * 最大重试次数
     */
    private Integer maxRetryTimes;

    private String lockId;

    private Date lockTime;

    /**
     * 备注
     */
    private String remk;

    private String sendResult;

    private Integer cost;

    private static final long serialVersionUID = 1L;
    public SendSms() {
    }

    public SendSms(String phone, String content, String dispatcherId) {
        this.phone = phone;
        this.content = content;
        this.dispatcherId = dispatcherId;
        this.status = 0;
        this.smsType = "0";
    }

    public SendSms(String phone, String content, String dispatcherId, String smsType) {
        this.phone = phone;
        this.content = content;
        this.dispatcherId = dispatcherId;
        this.smsType = smsType;
        this.status = 0;
    }

}