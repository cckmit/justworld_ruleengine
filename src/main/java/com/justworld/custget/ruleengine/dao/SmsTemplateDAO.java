package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.SmsTemplate;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsTemplateDAO {
    int deleteByPrimaryKey(String id);

    int insert(SmsTemplate record);

    int insertSelective(SmsTemplate record);

    SmsTemplate selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(SmsTemplate record);

    int updateByPrimaryKeyWithBLOBs(SmsTemplate record);

    int updateByPrimaryKey(SmsTemplate record);
}