package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.AiSmsJob;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiSmsJobDAO {

    @Insert("<script>" +
            "INSERT IGNORE INTO ai_sms_job (\n" +
            "  AI_SEQ,PHONE, AI_USERNAME, TAG, PHONE_STATUS, SHORT_URL_STATUS, STATUS, RULE_ID, CREATE_TIME, SMS_TEMPLATE_ID, SMS_TEMPLATE_URL, SMS_SHORT_URL\n" +
            ") \n" +
            "VALUES\n" +
            "<foreach item='aismsjob' index='index' collection='list' separator=',\n'>" +
            "  (\n" +
            "    #{aismsjob.aiSeq}, #{aismsjob.phone}, #{aismsjob.aiUsername}, #{aismsjob.tag},#{aismsjob.phoneStatus},#{aismsjob.shortUrlStatus},#{aismsjob.status}, #{aismsjob.ruleId}, #{aismsjob.createTime}, #{aismsjob.smsTemplateId}, #{aismsjob.smsTemplateUrl}, #{aismsjob.smsShortUrl}" +
            "  )\n" +
            "</foreach>" +
            "</script>")
    @Options(useGeneratedKeys=true)
    int insert(List<AiSmsJob> aiSmsJobList);

    @Select("SELECT A.*,P.PROVINCE,P.CITY,P.TEL_OPERATOR FROM AI_SMS_JOB A LEFT JOIN PHONE_IDENTIFY P ON A.PHONE=P.PHONE  WHERE A.ID=#{id}")
    AiSmsJob selectByPrimaryKey(@Param("id") Integer id);

    @Select("SELECT A.*,P.PROVINCE,P.CITY,P.TEL_OPERATOR FROM AI_SMS_JOB A LEFT JOIN PHONE_IDENTIFY P ON A.PHONE=P.PHONE  WHERE A.SEND_SMS_ID=#{sendSmsId}")
    AiSmsJob selectBySendSmsId(@Param("sendSmsId") Long sendSmsId);

    @Update("UPDATE AI_SMS_JOB SET PHONE_STATUS=#{phoneStatus} WHERE ID=#{id}")
    int updatePhoneStatus(AiSmsJob record);

    @Update("UPDATE AI_SMS_JOB SET SHORT_URL_STATUS=#{shortUrlStatus},SMS_TEMPLATE_URL=#{smsTemplateUrl},SMS_SHORT_URL=#{smsShortUrl} WHERE ID=#{id}")
    int updateShortUrlStatus(AiSmsJob record);

    @Select("SELECT A.*,P.PROVINCE,P.CITY,P.TEL_OPERATOR FROM AI_SMS_JOB A LEFT JOIN PHONE_IDENTIFY P ON A.PHONE=P.PHONE  WHERE A.ID=#{id} for update")
    AiSmsJob lockByPrimaryKey(@Param("id") Integer id);

    @Update("UPDATE `ai_sms_job` \n" +
            "SET\n" +
            "  `TAG` = #{tag}, `PHONE_STATUS` = #{phoneStatus}, `SHORT_URL_STATUS` = #{shortUrlStatus}, `STATUS` = #{status}, `RULE_ID` = #{ruleId}, `SMS_TEMPLATE_URL` = #{smsTemplateUrl}, `SMS_SHORT_URL` = #{smsShortUrl}, `SEND_SMS_ID` = #{sendSmsId} \n" +
            "WHERE `ID` = #{id}")
    int updateByPrimaryKey(AiSmsJob record);


    @Select("<script>" +
            "SELECT A.*,S.CONTENT,S.DISPATCHER_ID,P.PROVINCE,P.CITY,P.TEL_OPERATOR FROM AI_SMS_JOB A JOIN SEND_SMS S ON A.SEND_SMS_ID=S.ID LEFT JOIN PHONE_IDENTIFY P ON A.PHONE=P.PHONE " +
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

    @Select("<script>" +
            "SELECT A.*,S.CONTENT smsTemplateContent FROM AI_SMS_JOB A JOIN SMS_TEMPLATE S ON A.SMS_TEMPLATE_ID=S.ID " +
            "WHERE A.ID IN " +
            "<foreach item='id' index='index' collection='idList' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<AiSmsJob> queryListByIds(@Param("idList")List<Integer> idList);

    @Update("UPDATE `ai_sms_job` a JOIN send_sms b ON a.`SEND_SMS_ID`=b.`ID`\n" +
            "SET a.`status`=#{status}\n" +
            "WHERE b.DISPATCHER_ID=#{dispatcherId} AND b.LOCK_ID=#{lockId} AND b.STATUS='9'")
    int updateJobBySendSmsStatus(@Param("status")String status, @Param("dispatcherId")String dispatcherId, @Param("lockId")String lockId);

    @Update("UPDATE `ai_sms_job` a  \n" +
            "SET a.`status`=#{status}\n" +
            "WHERE SEND_SMS_ID=#{id}")
    int updateSingleJobBySendSmsStatus(@Param("status")String status, @Param("id")Long id);
}