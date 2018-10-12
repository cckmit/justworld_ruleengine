package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.AiSmsJob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AiSmsJobDAO {
    int deleteByPrimaryKey(String id);

    int insert(AiSmsJob record);

    int insertSelective(AiSmsJob record);

    AiSmsJob selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(AiSmsJob record);

    int updateByPrimaryKey(AiSmsJob record);

    @Select("SELECT * FROM AI_SMS_JOB")
    @ResultMap("BaseResultMap")
    List<AiSmsJob> queryList();
}