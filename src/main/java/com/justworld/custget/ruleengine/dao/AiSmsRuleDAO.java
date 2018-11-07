package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.AiSmsRule;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiSmsRuleDAO {
    int deleteByPrimaryKey(Integer id);

    int insert(AiSmsRule record);

    @Select("SELECT * FROM AI_SMS_RULE WHERE RULE_TYPE=#{ruleType} AND RULE_KEY=#{ruleKey}")
    List<AiSmsRule> queryValidAiSmsRuleList(@Param("ruleType") String ruleType, @Param("ruleKey")String ruleKey);

    AiSmsRule selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AiSmsRule record);

    int updateByPrimaryKey(AiSmsRule record);
}