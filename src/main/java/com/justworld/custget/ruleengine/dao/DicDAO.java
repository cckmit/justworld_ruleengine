package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.Dic;
import org.springframework.stereotype.Repository;

@Repository
public interface DicDAO {
    int deleteByPrimaryKey(Integer id);

    int insert(Dic record);

    int insertSelective(Dic record);

    Dic selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Dic record);

    int updateByPrimaryKey(Dic record);
}