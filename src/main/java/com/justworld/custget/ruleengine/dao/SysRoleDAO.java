package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.SysRole;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.FetchType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysRoleDAO {
    int deleteByPrimaryKey(Integer roleId);

    int insert(SysRole record);

    @Select("SELECT * FROM SYS_ROLE WHERE ROLE_ID=#{roleId}")
    @Results({
            @Result(column = "ROLE_ID",property = "authList",many = @Many(select="com.justworld.custget.ruleengine.dao.SysAuthDAO.queryBySysRoleId",fetchType = FetchType.LAZY))
    })
    SysRole selectByPrimaryKey(Integer roleId);

    @Select("SELECT * FROM SYS_ROLE WHERE ROLE_ID IN (SELECT ROLE_ID FROM USER_ROLE WHERE USER_ID = #{userId})")
    @Results({
            @Result(column = "ROLE_ID",property = "authList",many = @Many(select="com.justworld.custget.ruleengine.dao.SysAuthDAO.queryBySysRoleId",fetchType = FetchType.LAZY))
    })
    List<SysRole> queryBySysUserId(Integer userId);

    int updateByPrimaryKey(SysRole record);
}