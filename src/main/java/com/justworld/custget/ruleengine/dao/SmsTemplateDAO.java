package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.SmsTemplate;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmsTemplateDAO {
    int deleteByPrimaryKey(String id);

    @Insert("insert into sms_template (ID,TEMPLATE_NAME, REMK, CONTENT\n" +
            "      )\n" +
            "    values (#{id},#{templateName}, #{remk}, #{content}\n" +
            "      )")
    int insert(SmsTemplate record);

    @Select("<script>" +
            "SELECT * FROM SMS_TEMPLATE A " +
            "<where>" +
            "<if test='id != null and id !=\"\"'> " +
            "AND A.ID = #{id}" +
            "</if>" +
            "<if test='templateName != null and templateName !=\"\"'> " +
            "<bind name='templateName' value=\"'%'+templateName+'%'\"/>" +
            "AND A.TEMPLATE_NAME LIKE #{templateName}" +
            "</if>" +
            "<if test='content != null and content !=\"\"'> " +
            "<bind name='content' value=\"'%'+content+'%'\"/>" +
            "AND A.CONTENT LIKE #{content}" +
            "</if>" +
            "</where>" +
            "ORDER BY A.ID ASC" +
            "</script>")
    List<SmsTemplate> queryList(SmsTemplate cond);

    @Delete("<script>" +
            "DELETE FROM SMS_TEMPLATE " +
            "WHERE ID IN " +
            "<foreach item='id' index='index' collection='idList' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchDelete(@Param("idList") List idList);

    SmsTemplate selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(SmsTemplate record);

    int updateByPrimaryKey(SmsTemplate record);
}