package com.justworld.custget.ruleengine.service.bo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * ai_sms_job
 * @author 
 */
public class AiSmsJob implements Serializable {
    private Integer id;

    private String aiSeq;
    /**
     * 手机号
     */
    private String phone;

    /**
     * AI登录账户
     */
    private String aiUsername;

    /**
     * AI标签
     */
    private String tag;

    private String phoneStatus;

    private String shortUrlStatus;

    /**
     * 状态：1=已接收，2=已识别号码，3=已匹配规则，4=已生成短信
     */
    private String status;

    /**
     * 短信发送规则ID
     */
    private String ruleId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 短信链接点击次数
     */
    private int clickCount;

    /**
     * 短信链接最后点击时间
     */
    private Timestamp clickTime;

    /**
     * 要发送的短信模板ID
     */
    private String smsTemplateId;

    private String smsTemplateUrl;

    private String smsShortUrl;

    private String sendSmsId;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAiSeq() {
        return aiSeq;
    }

    public void setAiSeq(String aiSeq) {
        this.aiSeq = aiSeq;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAiUsername() {
        return aiUsername;
    }

    public void setAiUsername(String aiUsername) {
        this.aiUsername = aiUsername;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getPhoneStatus() {
        return phoneStatus;
    }

    public void setPhoneStatus(String phoneStatus) {
        this.phoneStatus = phoneStatus;
    }

    public String getShortUrlStatus() {
        return shortUrlStatus;
    }

    public void setShortUrlStatus(String shortUrlStatus) {
        this.shortUrlStatus = shortUrlStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getClickCount() {
        return clickCount;
    }

    public void setClickCount(int clickCount) {
        this.clickCount = clickCount;
    }

    public Timestamp getClickTime() {
        return clickTime;
    }

    public void setClickTime(Timestamp clickTime) {
        this.clickTime = clickTime;
    }

    public String getSmsTemplateId() {
        return smsTemplateId;
    }

    public void setSmsTemplateId(String smsTemplateId) {
        this.smsTemplateId = smsTemplateId;
    }

    public String getSmsTemplateUrl() {
        return smsTemplateUrl;
    }

    public void setSmsTemplateUrl(String smsTemplateUrl) {
        this.smsTemplateUrl = smsTemplateUrl;
    }

    public String getSmsShortUrl() {
        return smsShortUrl;
    }

    public void setSmsShortUrl(String smsShortUrl) {
        this.smsShortUrl = smsShortUrl;
    }

    public String getSendSmsId() {
        return sendSmsId;
    }

    public void setSendSmsId(String sendSmsId) {
        this.sendSmsId = sendSmsId;
    }
}