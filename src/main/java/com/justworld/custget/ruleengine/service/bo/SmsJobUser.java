package com.justworld.custget.ruleengine.service.bo;

import java.io.Serializable;

/**
 * sms_job_user
 * @author 
 */
public class SmsJobUser implements Serializable {
    /**
     * 昵称
     */
    private Integer id;

    /**
     * 用户类型：1=AI系统挂机短信接收用户
     */
    private String userType;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户密码密文
     */
    private String password;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}