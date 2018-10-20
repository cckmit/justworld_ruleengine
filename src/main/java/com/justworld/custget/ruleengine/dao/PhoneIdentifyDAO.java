package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.PhoneIdentify;
import org.springframework.stereotype.Repository;

@Repository
public interface PhoneIdentifyDAO {

    int insert(PhoneIdentify record);

    PhoneIdentify selectByPrimaryKey(String phone);

    int updateByPrimaryKey(PhoneIdentify record);
}