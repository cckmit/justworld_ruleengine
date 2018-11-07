package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.Notify;
import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;

@Repository
public interface NotifyDAO {
    int deleteByPrimaryKey(Integer id);

    @Insert("insert into notify (CREATE_TIME, NOTIFY_TYPE, `STATUS`, \n" +
            "      NOTIFY_KEY, CONTENT)\n" +
            "    values (now(), #{notifyType}, #{status}, \n" +
            "      #{notifyKey}, #{content})\n")
    int insert(Notify record);

    int insertSelective(Notify record);

    Notify selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Notify record);

    int updateByPrimaryKeyWithBLOBs(Notify record);

    int updateByPrimaryKey(Notify record);
}