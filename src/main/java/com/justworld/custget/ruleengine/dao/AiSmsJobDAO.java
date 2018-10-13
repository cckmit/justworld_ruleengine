package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.AiSmsJob;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper
@Repository
public interface AiSmsJobDAO {
    int deleteByPrimaryKey(String id);

    @Insert("INSERT INTO ai_sms_job (\n" +
            "  ID, PHONE, AI_USERNAME, TAG, STATUS, RULE_ID, CREATE_TIME, CLICK_COUNT, CLICK_TIME, SMS_TEMPLATE_ID\n" +
            ") \n" +
            "VALUES\n" +
            "  (\n" +
            "    #{id}, #{phone}, #{aiUsername}, #{tag},#{status}, #{ruleId}, #{createTime},#{clickCount}, #{clickTime}, #{smsTemplateId}" +
            "  )")
    int insert(AiSmsJob aiSmsJob);

    int insertSelective(AiSmsJob record);

    AiSmsJob selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(AiSmsJob record);

    int updateByPrimaryKey(AiSmsJob record);

    @Select("SELECT * FROM AI_SMS_JOB")
    List<AiSmsJob> queryList();
}