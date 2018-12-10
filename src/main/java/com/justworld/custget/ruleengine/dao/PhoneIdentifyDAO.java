package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.PhoneIdentify;
import org.springframework.stereotype.Repository;

@Repository
public interface PhoneIdentifyDAO {
    int deleteByPrimaryKey(String phone);

    int insert(PhoneIdentify record);

    int insertSelective(PhoneIdentify record);

    PhoneIdentify selectByPrimaryKey(String phone);

    int updateByPrimaryKeySelective(PhoneIdentify record);

    int updateByPrimaryKey(PhoneIdentify record);
}