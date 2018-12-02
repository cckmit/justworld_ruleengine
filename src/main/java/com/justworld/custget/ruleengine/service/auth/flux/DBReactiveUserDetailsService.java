package com.justworld.custget.ruleengine.service.auth.flux;

import com.justworld.custget.ruleengine.dao.SysUserDAO;
import com.justworld.custget.ruleengine.service.bo.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DBReactiveUserDetailsService implements ReactiveUserDetailsService {

    @Autowired
    private SysUserDAO sysUserDAO;

    @Override
    public Mono<UserDetails> findByUsername(String s) {
        SysUser user = sysUserDAO.selectByUsername(s);
        if(user == null){
            return Mono.error(new UsernameNotFoundException("用户名不正确"));
        }
        return Mono.just(user);
    }
}
