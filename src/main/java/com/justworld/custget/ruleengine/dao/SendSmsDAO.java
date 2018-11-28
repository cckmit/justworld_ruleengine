package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.SendSms;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SendSmsDAO {
    @Insert("insert into send_sms (PHONE, CONTENT, CREATE_TIME, \n" +
            "      SEND_TIME, `STATUS`, DONE_TIME, \n" +
            "      MSG_ID, SMS_TYPE, DISPATCHER_ID, \n" +
            "      RETRY_TIMES, MAX_RETRY_TIMES, REMK\n" +
            "      )\n" +
            "SELECT #{phone}, #{content}, now(), \n" +
            "      #{sendTime}, #{status}, #{doneTime}, \n" +
            "      #{msgId}, #{smsType}, #{dispatcherId}, \n" +
            "      0, MAX_RETRY_TIMES, #{remk}\n" +
            "      FROM SMS_TYPE_TIME WHERE TYPE_KEY=#{smsType}")
    @Options(useGeneratedKeys=true, keyColumn="id")
    int insert(SendSms record);

    @Select("SELECT * FROM SEND_SMS WHERE DISPATCHER_ID=#{dispatcherId} AND LOCK_ID=#{lockId} AND STATUS='9'")
    List<SendSms> queryLockedSendSmsList(@Param("dispatcherId") String dispatcherId, @Param("lockId")String lockId);

    @Update("UPDATE SEND_SMS SET STATUS='9',LOCK_ID=#{lockId},LOCK_TIME=now() " +
            "WHERE DISPATCHER_ID=#{dispatcherId} AND STATUS IN ('0','9') AND (STATUS='0' OR (STATUS='9' AND LOCK_TIME<date_add(now(),interval -30 minute))) " +
            "AND sms_type IN (SELECT type_key FROM sms_type_time WHERE DATE_FORMAT(NOW(),'%H%i%s')>=START_TIME AND DATE_FORMAT(NOW(),'%H%i%s')<=END_TIME) " +
            "limit #{limit}")
    int lockSendSms(@Param("dispatcherId") String dispatcherId, @Param("lockId")String lockId, @Param("limit")int limit);

    @Update("<script> " +
            "UPDATE SEND_SMS SET " +
            "<if test='retryTimes != null'> " +
            "STATUS=IF(RETRY_TIMES=MAX_RETRY_TIMES-1,'2',#{status})," +
            "RETRY_TIMES=RETRY_TIMES+1," +
            "</if>" +
            "<if test='retryTimes == null'> " +
            "STATUS=#{status}," +
            "</if>" +
            "<if test='msgId != null'> " +
            "MSG_ID=#{msgId}," +
            "</if>" +
            "<if test='remk != null'> " +
            "REMK=#{remk}," +
            "</if>" +
            "SEND_TIME=now()" +
            " WHERE DISPATCHER_ID=#{dispatcherId} AND LOCK_ID=#{lockId} AND STATUS='9' " +
            " </script>")
    int updateAndUnLockSendSms(SendSms cond);

    @Update("<script> " +
            "UPDATE SEND_SMS SET " +
            "<if test='retryTimes != null'> " +
            "STATUS=IF(RETRY_TIMES=MAX_RETRY_TIMES-1,'2',#{status})," +
            "RETRY_TIMES=RETRY_TIMES+1," +
            "</if>" +
            "<if test='retryTimes == null'> " +
            "STATUS=#{status}," +
            "</if>" +
            "MSG_ID=#{msgId},"+
            "REMK=#{remk}"+
            " WHERE DISPATCHER_ID=#{dispatcherId} AND LOCK_ID=#{lockId} AND PHONE=#{phone} " +
            " </script>")
    int updateSendSmsSendResult(SendSms cond);
}