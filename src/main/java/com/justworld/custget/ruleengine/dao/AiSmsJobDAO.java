package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.AiSmsJob;
import org.apache.ibatis.annotations.*;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Repository
public interface AiSmsJobDAO {

    @Insert("INSERT INTO ai_sms_job (\n" +
            "  AI_SEQ,PHONE, AI_USERNAME, TAG, PHONE_STATUS, SHORT_URL_STATUS, STATUS, RULE_ID, CREATE_TIME, CLICK_COUNT, CLICK_TIME, SMS_TEMPLATE_ID, SMS_TEMPLATE_URL, SMS_SHORT_URL\n" +
            ") \n" +
            "VALUES\n" +
            "  (\n" +
            "    #{aiSeq}, #{phone}, #{aiUsername}, #{tag},#{phoneStatus},#{shortUrlStatus},#{status}, #{ruleId}, #{createTime},#{clickCount}, #{clickTime}, #{smsTemplateId}, #{smsTemplateUrl}, #{smsShortUrl}" +
            "  )")
    @Options(useGeneratedKeys=true, keyColumn="id")
    int insert(AiSmsJob aiSmsJob);

    @Select("SELECT A.*,P.PROVINCE,P.CITY,P.TEL_OPERATOR FROM AI_SMS_JOB A LEFT JOIN PHONE_IDENTIFY P ON A.PHONE=P.PHONE  WHERE A.ID=#{id}")
    AiSmsJob selectByPrimaryKey(@Param("id") Integer id);

    @Select("SELECT * FROM AI_SMS_JOB WHERE AI_SEQ=#{aiSeq}")
    AiSmsJob selectByAiSeq(@Param("aiSeq") String aiSeq);

    @Select("SELECT A.*,P.PROVINCE,P.CITY,P.TEL_OPERATOR FROM AI_SMS_JOB A LEFT JOIN PHONE_IDENTIFY P ON A.PHONE=P.PHONE  WHERE A.ID=#{id} for update")
    AiSmsJob lockByPrimaryKey(@Param("id") Integer id);

    @Update("UPDATE `ai_sms_job` \n" +
            "SET\n" +
            "  `TAG` = #{tag}, `PHONE_STATUS` = #{phoneStatus}, `SHORT_URL_STATUS` = #{shortUrlStatus}, `STATUS` = #{status}, `RULE_ID` = #{ruleId}, `CLICK_COUNT` = #{clickCount}, `CLICK_TIME` = #{clickTime}, `SMS_TEMPLATE_URL` = #{smsTemplateUrl}, `SMS_SHORT_URL` = #{smsShortUrl}, `SEND_SMS_ID` = #{sendSmsId} \n" +
            "WHERE `ID` = #{id}")
    int updateByPrimaryKey(AiSmsJob record);


    @Select("<script>" +
            "SELECT A.*,P.PROVINCE,P.CITY,P.TEL_OPERATOR FROM AI_SMS_JOB A LEFT JOIN PHONE_IDENTIFY P ON A.PHONE=P.PHONE " +
            "<where>" +
            "<if test='id != null and id !=\"\"'> " +
            "AND A.ID = #{id}" +
            "</if>" +
            "<if test='phone != null and phone !=\"\"'> " +
            "<bind name='phone' value=\"'%'+phone+'%'\"/>" +
            "AND A.PHONE LIKE #{phone}" +
            "</if>" +
            "</where>" +
            "ORDER BY A.ID DESC" +
            "</script>")
    List<AiSmsJob> queryList(AiSmsJob cond);
}