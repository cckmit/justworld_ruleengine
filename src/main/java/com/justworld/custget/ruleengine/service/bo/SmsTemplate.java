package com.justworld.custget.ruleengine.service.bo;

import java.io.Serializable;

/**
 * sms_template
 * @author 
 */
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

    private static final long serialVersionUID = 1L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getRemk() {
        return remk;
    }

    public void setRemk(String remk) {
        this.remk = remk;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}