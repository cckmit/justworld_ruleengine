package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.SmsDispatcher;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmsDispatcherDAO {
    int deleteByPrimaryKey(String dispatcherKey);

    int insert(SmsDispatcher record);

    int insertSelective(SmsDispatcher record);

    @Select("SELECT * FROM SMS_DISPATCHER WHERE DISPATCHER_KEY=#{dispatcherKey}")
    SmsDispatcher selectByPrimaryKey(String dispatcherKey);

    @Select("SELECT * FROM SMS_DISPATCHER ")
    List<SmsDispatcher> queryAllSmsDispatcherList();

    int updateByPrimaryKeySelective(SmsDispatcher record);

    int updateByPrimaryKey(SmsDispatcher record);
}