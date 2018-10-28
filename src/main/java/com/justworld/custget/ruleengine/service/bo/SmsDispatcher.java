package com.justworld.custget.ruleengine.service.bo;

import java.io.Serializable;

/**
 * sms_dispatcher
 * @author 
 */
public class SmsDispatcher implements Serializable {
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

    public String getDispatcherKey() {
        return dispatcherKey;
    }

    public void setDispatcherKey(String dispatcherKey) {
        this.dispatcherKey = dispatcherKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemk() {
        return remk;
    }

    public void setRemk(String remk) {
        this.remk = remk;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}