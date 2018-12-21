package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.SmsClickStat;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsClickStatDAO {
    int deleteByPrimaryKey(Long id);

    int insert(SmsClickStat record);

    int insertSelective(SmsClickStat record);

    SmsClickStat selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SmsClickStat record);

    int updateByPrimaryKey(SmsClickStat record);
}