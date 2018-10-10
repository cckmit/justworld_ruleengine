package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.AiSmsJob;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiSmsJobDAO {
    int deleteByPrimaryKey(String id);

    int insert(AiSmsJob record);

    int insertSelective(AiSmsJob record);

    AiSmsJob selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(AiSmsJob record);

    int updateByPrimaryKey(AiSmsJob record);
}