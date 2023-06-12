package com.atguigu.auth.service;

import com.atguigu.model.system.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author lhy
 * @since 2023-05-26
 */
public interface SysUserService extends IService<SysUser> {
   void updateStatus(Long id,Integer status);

   //根据用户名进行查询
    SysUser getByUsername(String username);

    /*我的*/
    Map<String, Object> getCurrentUser();
}
