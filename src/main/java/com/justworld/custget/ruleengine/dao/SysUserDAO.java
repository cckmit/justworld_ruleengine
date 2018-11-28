package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.SysUser;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.FetchType;
import org.springframework.stereotype.Repository;

@Repository
public interface SysUserDAO {
    int deleteByPrimaryKey(Integer userId);

    int insert(SysUser record);

    int insertSelective(SysUser record);

    @Select("SELECT * FROM SYS_USER WHERE USERNAME=#{username}")
    @Results({
            @Result(column = "USER_ID",property = "roleList",many = @Many(select="com.justworld.custget.ruleengine.dao.SysRoleDAO.queryBySysUserId",fetchType = FetchType.EAGER)),
            @Result(column = "USER_ID",property = "authList",many = @Many(select="com.justworld.custget.ruleengine.dao.SysAuthDAO.queryBySysUserId",fetchType = FetchType.EAGER))
    })
    SysUser selectByUsername(String username);

    int updateByPrimaryKeySelective(SysUser record);

    int updateByPrimaryKey(SysUser record);
}