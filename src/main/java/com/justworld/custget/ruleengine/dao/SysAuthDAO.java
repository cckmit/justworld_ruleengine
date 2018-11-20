package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.SysAuth;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysAuthDAO {
    int deleteByPrimaryKey(Integer authId);

    int insert(SysAuth record);

    int insertSelective(SysAuth record);

    SysAuth selectByPrimaryKey(Integer authId);

    int updateByPrimaryKeySelective(SysAuth record);

    @Select("SELECT * FROM SYS_AUTH WHERE AUTH_ID IN (SELECT AUTH_ID FROM ROLE_AUTH WHERE ROLE_ID=#{roleId})")
    List<SysAuth> queryBySysRoleId(Integer roleId);

    @Select("SELECT * FROM SYS_AUTH WHERE AUTH_ID IN (SELECT AUTH_ID FROM ROLE_AUTH WHERE ROLE_ID IN (SELECT ROLE_ID FROM USER_ROLE WHERE USER_ID = #{userId}))")
    List<SysAuth> queryBySysUserId(Integer userId);
}