package com.justworld.custget.ruleengine.service.bo;

import java.io.Serializable;

/**
 * base_config
 * @author 
 */
public class BaseConfig implements Serializable {
    /**
     * 配置组别
     */
    private String cfgGroup;

    /**
     * 组内配置键值
     */
    private String cfgKey;

    /**
     * 配置名称
     */
    private String name;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 配置值说明
     */
    private String desc;

    /**
     * 配置值
     */
    private String cfgValue;

    private static final long serialVersionUID = 1L;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCfgValue() {
        return cfgValue;
    }

    public void setCfgValue(String cfgValue) {
        this.cfgValue = cfgValue;
    }
}