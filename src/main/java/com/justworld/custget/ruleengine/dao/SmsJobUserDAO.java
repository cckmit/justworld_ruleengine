package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.SmsJobUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsJobUserDAO {
    int deleteByPrimaryKey(Integer id);

    int insert(SmsJobUser record);

    int insertSelective(SmsJobUser record);

    SmsJobUser selectByPrimaryKey(Integer id);

    @Select("SELECT * FROM SMS_JOB_USER WHERE USER_TYPE=#{userType} AND USERNAME=#{username}")
    SmsJobUser selectByUsername(@Param("userType") String userType, @Param("username") String username);

    int updateByPrimaryKeySelective(SmsJobUser record);

    int updateByPrimaryKey(SmsJobUser record);
}