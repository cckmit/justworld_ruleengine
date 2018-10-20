package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.SendSms;
import org.springframework.stereotype.Repository;

@Repository
public interface SendSmsDAO {
    int deleteByPrimaryKey(Integer id);

    int insert(SendSms record);

    int insertSelective(SendSms record);

    SendSms selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SendSms record);

    int updateByPrimaryKey(SendSms record);
}