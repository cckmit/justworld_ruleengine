package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.AiSmsRule;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiSmsRuleDAO {
    @Delete("DELETE FROM AI_SMS_RULE WHERE RULE_TYPE=#{ruleType} AND RULE_KEY=#{ruleKey}")
    int deleteRule(@Param("ruleType") String ruleType, @Param("ruleKey")String ruleKey);

    int insert(AiSmsRule record);

    @Select("SELECT * FROM AI_SMS_RULE WHERE RULE_TYPE=#{ruleType} AND RULE_KEY=#{ruleKey}")
    List<AiSmsRule> queryValidAiSmsRuleList(@Param("ruleType") String ruleType, @Param("ruleKey")String ruleKey);

    AiSmsRule selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AiSmsRule record);

    int updateByPrimaryKey(AiSmsRule record);
}