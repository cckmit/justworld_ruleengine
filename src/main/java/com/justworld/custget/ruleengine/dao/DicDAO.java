package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.Dic;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DicDAO {
    int deleteByPrimaryKey(Integer id);

    @Insert("insert into dic (GROUP_NAME, DIC_GROUP, PID, \n" +
            "      DIC_NAME, DIC_INFO, DIC_VALUE\n" +
            "      )\n" +
            "    values (#{groupName}, #{dicGroup}, #{pid}, \n" +
            "      #{dicName}, #{dicInfo}, #{dicValue}\n" +
            "      )")
    @Options(useGeneratedKeys=true,keyColumn = "id")
    int insert(Dic record);

    @Select("SELECT * FROM DIC WHERE DIC_GROUP=#{dicGroup}")
    List<Dic> queryDicGroup(String dicGroup);

    Dic selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Dic record);

    int updateByPrimaryKey(Dic record);
}