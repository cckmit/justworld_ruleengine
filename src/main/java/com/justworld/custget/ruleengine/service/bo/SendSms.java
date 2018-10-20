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
     * 行号
     */
    private String bkno;

    /**
     * 被叫号码
     */
    private String phone;

    private String content;

    private Date createTime;

    private Date sendTime;

    /**
     * 状态
     */
    private Byte status;

    private Date doneTime;

    /**
     * 移动网关返回的短信ID
     */
    private String msgId;

    /**
     * 短信发送类型，用于定义该短信的发送时间
     */
    private String smsType;

    /**
     * 短信发送类型，1-内网，移动短信，2-异网，电信联通
     */
    private String sendType;

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBkno() {
        return bkno;
    }

    public void setBkno(String bkno) {
        this.bkno = bkno;
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

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
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

    public String getSendType() {
        return sendType;
    }

    public void setSendType(String sendType) {
        this.sendType = sendType;
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