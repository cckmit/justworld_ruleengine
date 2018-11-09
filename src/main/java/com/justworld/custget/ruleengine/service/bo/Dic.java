package com.justworld.custget.ruleengine.service.bo;

import java.io.Serializable;

/**
 * dic
 * @author 
 */
public class Dic implements Serializable {
    /**
     * 字典ID
     */
    private Integer id;

    /**
     * 类型名称
     */
    private String groupName;

    /**
     * 字典类型
     */
    private String dicGroup;

    /**
     * 父ID
     */
    private Integer pid;

    /**
     * 字典名称
     */
    private String dicName;

    /**
     * 字典信息
     */
    private String dicInfo;

    /**
     * 字典值
     */
    private String dicValue;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDicGroup() {
        return dicGroup;
    }

    public void setDicGroup(String dicGroup) {
        this.dicGroup = dicGroup;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getDicName() {
        return dicName;
    }

    public void setDicName(String dicName) {
        this.dicName = dicName;
    }

    public String getDicInfo() {
        return dicInfo;
    }

    public void setDicInfo(String dicInfo) {
        this.dicInfo = dicInfo;
    }

    public String getDicValue() {
        return dicValue;
    }

    public void setDicValue(String dicValue) {
        this.dicValue = dicValue;
    }
}