package com.justworld.custget.ruleengine.service.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * sys_role
 * @author 
 */
@Data
public class SysRole implements Serializable {
    private Integer roleId;

    private String roleName;

    private String remk;

    private List<SysAuth> authList;

    private static final long serialVersionUID = 1L;
}