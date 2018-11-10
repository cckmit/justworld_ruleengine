package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.BaseConfig;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

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
            "  `CFG_VALUE` = #{cfgValue} \n" +
            "WHERE `CFG_GROUP` = #{cfgGroup} \n" +
            "  AND `CFG_KEY` = #{cfgKey}")
    int updateByPrimaryKey(BaseConfig record);

    @Select("<script>" +
            "SELECT * FROM `base_config` " +
            "<where> " +
            "<if test='cfgGroup != null and cfgGroup !=\"\"'> " +
            "CFG_GROUP=#{cfgGroup}" +
            "</if>" +
            "</where> " +
            "ORDER BY CFG_GROUP,CFG_KEY" +
            "</script>")
    List<BaseConfig> queryGroup(@Param("cfgGroup") String cfgGroup);
}