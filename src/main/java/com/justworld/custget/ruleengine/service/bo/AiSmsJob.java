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
    private String id;

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
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 短信链接点击次数
     */
    private String clickCount;

    /**
     * 短信链接最后点击时间
     */
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp clickTime;

    /**
     * 要发送的短信模板ID
     */
    private String smsTemplateId;

    private static final long serialVersionUID = 1L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getClickCount() {
        return clickCount;
    }

    public void setClickCount(String clickCount) {
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
}