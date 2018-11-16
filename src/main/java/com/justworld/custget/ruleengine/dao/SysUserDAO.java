package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.SysUser;
import org.springframework.stereotype.Repository;

@Repository
public interface SysUserDAO {
    int deleteByPrimaryKey(String userId);

    int insert(SysUser record);

    int insertSelective(SysUser record);

    SysUser selectByPrimaryKey(String userId);

    int updateByPrimaryKeySelective(SysUser record);

    int updateByPrimaryKey(SysUser record);
}