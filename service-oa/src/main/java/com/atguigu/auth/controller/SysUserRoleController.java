package com.atguigu.auth.controller;


import com.atguigu.auth.service.SysUserRoleService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * sysUserRole 用户分配角色 前端控制器
 * </p>
 *
 * @author lhy
 * @since 2023-05-26
 */
@Api(tags = "用户角色管理")
@RestController
@RequestMapping("/admin/system/sysUserRole")
public class SysUserRoleController {
   @Autowired
    private SysUserRoleService sysUserRoleService;


}

