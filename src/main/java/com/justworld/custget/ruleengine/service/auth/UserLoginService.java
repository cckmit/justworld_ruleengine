package com.justworld.custget.ruleengine.service.auth;

import com.justworld.custget.ruleengine.dao.SysUserDAO;
import com.justworld.custget.ruleengine.service.bo.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserLoginService implements UserDetailsService{

    @Autowired
    private SysUserDAO sysUserDAO;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        SysUser user = sysUserDAO.selectByUsername(s);
        if(user == null){
            throw new UsernameNotFoundException("用户名不正确");
        }
        return user;
    }
}
