package com.justworld.custget.ruleengine.service.bo;

import java.io.Serializable;
import java.util.Date;

/**
 * notify
 * @author 
 */
public class Notify implements Serializable {

    /**
     * 生成短信渠道类通知
     * @param key
     * @param content
     * @return
     */
    public static Notify createDispatcherNotify(String key, String content){
        Notify notify = new Notify();
        notify.setNotifyType("SMS_DISPATCHER");
        notify.setStatus("0");
        notify.setNotifyKey(key);
        notify.setContent(content);
        return notify;
    }

    private Integer id;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 通知类型
     */
    private String notifyType;

    /**
     * 状态1=正常，2=已处理
     */
    private String status;

    /**
     * 通知键值
     */
    private String notifyKey;

    /**
     * 通知内容
     */
    private String content;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getNotifyType() {
        return notifyType;
    }

    public void setNotifyType(String notifyType) {
        this.notifyType = notifyType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotifyKey() {
        return notifyKey;
    }

    public void setNotifyKey(String notifyKey) {
        this.notifyKey = notifyKey;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}