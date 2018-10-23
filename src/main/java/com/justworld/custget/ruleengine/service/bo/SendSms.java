package com.justworld.custget.ruleengine.service.bo;

import java.io.Serializable;
import java.util.Date;

/**
 * send_sms
 * @author 
 */
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

    /**
     * 备注
     */
    private String remk;

    private static final long serialVersionUID = 1L;
    public SendSms() {
    }

    public SendSms(String phone, String content, String dispatcherId) {
        this.phone = phone;
        this.content = content;
        this.dispatcherId = dispatcherId;
        this.status = 0;
    }

    public SendSms(String phone, String content, String dispatcherId, String smsType) {
        this.phone = phone;
        this.content = content;
        this.smsType = smsType;
        this.status = 0;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getDoneTime() {
        return doneTime;
    }

    public void setDoneTime(Date doneTime) {
        this.doneTime = doneTime;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getSmsType() {
        return smsType;
    }

    public void setSmsType(String smsType) {
        this.smsType = smsType;
    }

    public String getDispatcherId() {
        return dispatcherId;
    }

    public void setDispatcherId(String dispatcherId) {
        this.dispatcherId = dispatcherId;
    }

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(Integer retryTimes) {
        this.retryTimes = retryTimes;
    }

    public Integer getMaxRetryTimes() {
        return maxRetryTimes;
    }

    public void setMaxRetryTimes(Integer maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

    public String getRemk() {
        return remk;
    }

    public void setRemk(String remk) {
        this.remk = remk;
    }
}