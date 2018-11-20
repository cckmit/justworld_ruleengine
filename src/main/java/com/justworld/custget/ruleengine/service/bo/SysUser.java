package com.justworld.custget.ruleengine.service.bo;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * sys_user
 * @author 
 */
@Data
public class SysUser implements Serializable, UserDetails {
    private Integer userId;

    private String username;

    private String password;

    private Date createTime;

    private Date lastLogin;

    private List<SysRole> roleList;

    private List<SysAuth> authList;

    /**
     * 1=可用，0=停用
     */
    private String status;

    private static final long serialVersionUID = 1L;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authList;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}