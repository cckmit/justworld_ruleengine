package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.ShortUrlDefine;
import org.springframework.stereotype.Repository;

@Repository
public interface ShortUrlDefineDAO {
    int deleteByPrimaryKey(Long id);

    int insert(ShortUrlDefine record);

    int insertSelective(ShortUrlDefine record);

    ShortUrlDefine selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ShortUrlDefine record);

    int updateByPrimaryKey(ShortUrlDefine record);
}