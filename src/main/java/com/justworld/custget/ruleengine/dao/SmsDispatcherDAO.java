package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.SmsDispatcher;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsDispatcherDAO {
    int deleteByPrimaryKey(String dispatcherKey);

    int insert(SmsDispatcher record);

    int insertSelective(SmsDispatcher record);

    SmsDispatcher selectByPrimaryKey(String dispatcherKey);

    int updateByPrimaryKeySelective(SmsDispatcher record);

    int updateByPrimaryKey(SmsDispatcher record);
}