package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.Region;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegionDAO {
    @Select("SELECT * FROM REGION WHERE LEVEL<=#{maxLevel}")
    List<Region> queryList(int maxLevel);
}