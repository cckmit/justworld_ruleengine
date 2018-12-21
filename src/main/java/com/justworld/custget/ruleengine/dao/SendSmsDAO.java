package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.SendSms;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SendSmsDAO {
    @Select("SELECT S.*,T.URL FROM SEND_SMS S LEFT JOIN SMS_TEMPLATE T ON S.SMS_TEMPLATE_ID = T.ID WHERE S.ID=#{id}")
    SendSms load(@Param("id") Long id);

    @Insert("insert into send_sms (PHONE, CONTENT, CREATE_TIME, \n" +
            "      SEND_TIME, `STATUS`, DONE_TIME, \n" +
            "      MSG_ID, SMS_TYPE, DISPATCHER_ID, \n" +
            "      RETRY_TIMES, MAX_RETRY_TIMES, REMK, SEND_RESULT,\n" +
            "      CLICK_COUNT,SMS_TEMPLATE_ID" +
            "      )\n" +
            "SELECT #{phone}, #{content}, now(), \n" +
            "      #{sendTime}, #{status}, #{doneTime}, \n" +
            "      #{msgId}, #{smsType}, #{dispatcherId}, \n" +
            "      0, MAX_RETRY_TIMES, #{remk},'0',\n" +
            "      0,#{smsTemplateId}" +
            "      FROM SMS_TYPE_TIME WHERE TYPE_KEY=#{smsType}")
    @Options(useGeneratedKeys=true, keyColumn="id")
    int insert(SendSms record);

    @Select("<script> " +
            "SELECT * FROM SEND_SMS " +
            "WHERE " +
            "LOCK_ID=#{lockId} AND STATUS='9'" +
            " AND DISPATCHER_ID=#{dispatcherId}  " +
            "</script>")
    /**
     * 查询锁定记录
     */
    List<SendSms> queryLockedSendSmsList(@Param("dispatcherId") String dispatcherId, @Param("lockId")String lockId);

    @Update("<script> " +
            "UPDATE SEND_SMS SET STATUS='9',LOCK_ID=#{lockId},LOCK_TIME=now() " +
            "WHERE  STATUS = 0  " +
            " AND DISPATCHER_ID=#{dispatcherId}  " +
            "limit #{limit}" +
            " </script>")
    /**
     * 锁定要分配渠道的记录
     */
    int lockSendSmsForDecideDispatcher(@Param("dispatcherId") String dispatcherId, @Param("lockId")String lockId, @Param("limit")int limit);

    @Update("<script> " +
            "UPDATE SEND_SMS SET STATUS='9',LOCK_ID=#{lockId},LOCK_TIME=now() " +
            "WHERE  STATUS = 0 " +
            "AND DISPATCHER_ID=#{dispatcherId} AND RETRY_TIMES <![CDATA[  < ]]>MAX_RETRY_TIMES " +
            "AND (SEND_TIME IS NULL OR SEND_TIME <![CDATA[  < ]]> date_add(now(),interval -10 minute)) " +
            "AND sms_type IN (SELECT type_key FROM sms_type_time WHERE DATE_FORMAT(NOW(),'%H%i%s')<![CDATA[>=]]>START_TIME AND DATE_FORMAT(NOW(),'%H%i%s')<![CDATA[<=]]>END_TIME) " +
            "limit #{limit}" +
            " </script>")
    /**
     * 锁定要发送的短信
     */
    int lockSendSms(@Param("dispatcherId") String dispatcherId, @Param("lockId")String lockId, @Param("limit")int limit);

    @Update("<script> " +
            "UPDATE SEND_SMS SET STATUS='9',LOCK_ID=#{lockId},LOCK_TIME=now() " +
            "WHERE DISPATCHER_ID=#{dispatcherId} AND STATUS =0  " +
            "AND sms_type IN (SELECT type_key FROM sms_type_time WHERE DATE_FORMAT(NOW(),'%H%i%s') <![CDATA[ >= ]]> START_TIME AND DATE_FORMAT(NOW(),'%H%i%s') <![CDATA[ <= ]]> END_TIME) " +
            "AND ID IN " +
            "<foreach item='sendSms' index='index' collection='sendSmsList' open='(' separator=',' close=')'>" +
            "#{sendSms.id}" +
            "</foreach> " +
            " </script>")
    int lockSendSmsByIds(@Param("dispatcherId") String dispatcherId, @Param("lockId")String lockId, @Param("sendSmsList")List<SendSms> sendSmsList);

    @Update("" +
            "UPDATE SEND_SMS SET " +
            "DISPATCHER_ID=#{dispatcherId}," +
            "STATUS=#{status}" +
            " WHERE ID=#{id} " +
            "")
    int updateDispatcherAndUnlock(SendSms cond);

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
            "SEND_TIME=#{sendTime},"+
            "REMK=#{remk},COST=#{cost}"+
            " WHERE ID=#{id} " +
            " </script>")
    int updateSendSmsSendResult(SendSms cond);

    @Select("SELECT COUNT(1) FROM SEND_SMS WHERE DISPATCHER_ID=#{dispatcherId} AND STATUS='1' AND SEND_TIME>date_add(now(),interval -30 minute) AND SEND_RESULT='0'")
    int countSmsForReport(String dispatcherId);

    @Update("UPDATE SEND_SMS SET SEND_RESULT=#{sendResult},REMK=#{remk},DONE_TIME=#{doneTime} WHERE MSG_ID=#{msgId} AND PHONE=#{phone}")
    int updateSendResult(SendSms cond);

    @Update("UPDATE SEND_SMS SET CLICK_COUNT=#{clickCount},CLICK_TIME=#{clickTime} WHERE ID=#{id}")
    int updateClickInfo(SendSms cond);

    @Update("UPDATE SEND_SMS SET STATUS=0 WHERE LOCK_TIME < date_add(now(),interval -30 minute) AND STATUS=9")
    int unlockOvertimeRecord();

}