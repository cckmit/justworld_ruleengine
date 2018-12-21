package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.SmsClickLog;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsClickLogDAO {
    int deleteByPrimaryKey(Long id);

    int insert(SmsClickLog record);

    int insertSelective(SmsClickLog record);

    SmsClickLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SmsClickLog record);

    int updateByPrimaryKey(SmsClickLog record);
}