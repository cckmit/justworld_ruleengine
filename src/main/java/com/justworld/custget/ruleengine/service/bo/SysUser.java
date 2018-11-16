package com.justworld.custget.ruleengine.service.bo;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * sys_user
 * @author 
 */
@Data
public class SysUser implements Serializable, UserDetails {
    private String userId;

    private String username;

    private String password;

    private Date createTime;

    private Date lastLogin;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    /**
     * 1=可用，0=停用
     */
    private String status;

    private static final long serialVersionUID = 1L;
}