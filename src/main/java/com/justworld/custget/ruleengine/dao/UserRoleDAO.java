package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.UserRoleKey;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleDAO {
    int deleteByPrimaryKey(UserRoleKey key);

    int insert(UserRoleKey record);

    int insertSelective(UserRoleKey record);
}