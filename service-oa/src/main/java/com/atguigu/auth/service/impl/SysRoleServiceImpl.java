package com.atguigu.auth.service.impl;

import com.atguigu.auth.mapper.SysRoleMapper;
import com.atguigu.auth.service.SysRoleService;
import com.atguigu.auth.service.SysUserRoleService;
import com.atguigu.model.system.SysRole;
import com.atguigu.model.system.SysUserRole;
import com.atguigu.vo.system.AssginRoleVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl  extends ServiceImpl<SysRoleMapper,SysRole> implements SysRoleService {
    @Autowired
    private SysUserRoleService sysUserRoleService;

    /*1. 根据用户id 获取  角色列表  数据*/
    @Override
    public Map<String, Object> findRoleByAdminId(Long userId) {
        //1.查询所有角色，返回list集合
        List<SysRole> allRoleList = baseMapper.selectList(null);
        //2.根据userid查询角色用户关系表，查询userid对应所有角色id
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId,userId);
        List<SysUserRole> existUserRoleList = sysUserRoleService.list(wrapper);
        //从查询出来的用户id对应角色list集合，获取所有角色id
            /*
                List<Long> existRoleIdList = new ArrayList<>();
                for (SysUserRole sysUserRole : existUserRoleList) {
                    Long existRoleId = sysUserRole.getRoleId();
                    existRoleIdList.add(existRoleId);
                }
            */
        //存在角色 ID List                      //stream 0 + lambda表达式c->c.getRoleId()            放到list
        List<Long> existRoleIdList = existUserRoleList.stream().map(c->c.getRoleId()).collect(Collectors.toList());
        //3.根据查询所有角色id，找到对应角色信息
          //根据角色id到所有的角色的list集合进行比较
            //存在的角色列表
            List<SysRole> assginRoleList = new ArrayList<>();
            for (SysRole role : allRoleList) {
                //避免重复查询添加list
                if(existRoleIdList.contains(role.getId())){
                    assginRoleList.add(role);
                }
            }

        //4.把得到的两部分数据封装Map集合，返回
        Map<String, Object> roleMap = new HashMap<>();
        roleMap.put("assginRoleList", assginRoleList);
        roleMap.put("allRolesList", allRoleList);
        return roleMap;
    }
    /*2 为用户分配角色*/
    @Override
    public void doAssign(AssginRoleVo assginRoleVo) {
        //删除用户之前角色数据： 把用户之前分配角色数据删除，remove sUserRole by u_id
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId,assginRoleVo.getUserId());
        sysUserRoleService.remove(wrapper);

        //重新进行分配
        List<Long> roleIdList = assginRoleVo.getRoleIdList();
        for(Long roleId:roleIdList){
            if(StringUtils.isEmpty(roleId)){
                continue;
            }
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(assginRoleVo.getUserId());
            sysUserRole.setRoleId(roleId);
            sysUserRoleService.save(sysUserRole);
        }
    }

    /*
    不需要写 MybatisPlus已经写在ServiceImpl<M extends BaseMapper<T>, T>里了
                        只要继承他就好
    @Autowired
    protected M baseMapper; */
}
