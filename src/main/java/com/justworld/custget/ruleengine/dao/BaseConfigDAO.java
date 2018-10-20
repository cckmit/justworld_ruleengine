package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.BaseConfig;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface BaseConfigDAO {

    @Insert("INSERT INTO `base_config` (\n" +
            "  `CFG_GROUP`, `CFG_KEY`, `CFG_VALUE`, `NAME`, `GROUP_NAME`, `DESC`\n" +
            ") \n" +
            "VALUES\n" +
            "  (\n" +
            "    #{cfgGroup}, #{cfg_key}, #{cfg_value}, #{name}, #{group_name}, #{desc}\n" +
            "  )")
    int insert(BaseConfig record);

    @Select("SELECT * FROM `base_config` WHERE CFG_GROUP=#{cfgGroup} AND CFG_KEY=#{cfgKey}")
    BaseConfig selectByPrimaryKey(@Param("cfgGroup") String cfgGroup, @Param("cfgKey")String cfgKey);

    @Update("UPDATE \n" +
            "  `base_config` \n" +
            "SET\n" +
            "  `CFG_VALUE` = #{cfg_value}, `NAME` = #{name}, `GROUP_NAME` = #{group_name}, `DESC` = #{desc} \n" +
            "WHERE `CFG_GROUP` = #{cfgGroup} \n" +
            "  AND `CFG_KEY` = #{cfg_key}")
    int updateByPrimaryKey(BaseConfig record);
}