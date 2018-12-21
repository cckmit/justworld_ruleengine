package com.justworld.custget.ruleengine.service.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * sms_template
 * @author 
 */
@Data
public class SmsTemplate implements Serializable {
    /**
     * 短信模板ID
     */
    private String id;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板备注
     */
    private String remk;

    /**
     * 模板内容
     */
    private String content;

    private String url;

    private static final long serialVersionUID = 1L;
}