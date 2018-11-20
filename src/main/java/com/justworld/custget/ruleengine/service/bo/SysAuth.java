package com.justworld.custget.ruleengine.service.bo;

import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;

/**
 * sys_auth
 * @author 
 */
public class SysAuth implements Serializable, GrantedAuthority {
    private Integer authId;

    /**
     * 权限名
     */
    private String authName;

    /**
     * 权限描述
     */
    private String remk;

    private static final long serialVersionUID = 1L;

    @Override
    public String getAuthority() {
        return authName;
    }

    public Integer getAuthId() {
        return authId;
    }

    public void setAuthId(Integer authId) {
        this.authId = authId;
    }

    public String getAuthName() {
        return authName;
    }

    public void setAuthName(String authName) {
        this.authName = authName;
    }

    public String getRemk() {
        return remk;
    }

    public void setRemk(String remk) {
        this.remk = remk;
    }
}