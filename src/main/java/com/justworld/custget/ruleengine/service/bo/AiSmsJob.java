package com.justworld.custget.ruleengine.service.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * ai_sms_job
 * @author 
 */
@Data public class AiSmsJob implements Serializable {
    private String province;
    private String city;
    private String telOperator;
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
//    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 短信链接点击次数
     */
    private int clickCount;

    /**
     * 短信链接最后点击时间
     */
//    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp clickTime;

    /**
     * 要发送的短信模板ID
     */
    private String smsTemplateId;

    private String smsTemplateUrl;

    private String smsShortUrl;

    private String sendSmsId;

    private String smsTemplateContent;

    private static final long serialVersionUID = 1L;

}