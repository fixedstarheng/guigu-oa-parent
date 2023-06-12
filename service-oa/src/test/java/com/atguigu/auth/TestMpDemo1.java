package com.atguigu.auth;

import com.atguigu.auth.mapper.SysRoleMapper;
import com.atguigu.model.system.SysRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
/**
 * 测试 SysRole的Mapper
 * */
import java.util.List;

@SpringBootTest
public class TestMpDemo1 {
    @Autowired
    private SysRoleMapper sysRoleMapper;

    /*查询SysRole List 所有*/
    @Test
    public void getAll(){
        List<SysRole> sysRoles = sysRoleMapper.selectList(null);
        System.out.println(sysRoles);
    }

    /*添加*/
    @Test
    public void addSysRole(){
        SysRole sysRole = new SysRole();
        sysRole.setRoleName("测试");
        sysRole.setRoleCode("test");
        sysRole.setDescription("测试");
        int insert = sysRoleMapper.insert(sysRole);
        System.out.println("添加响应结果： "+insert);
    }
    /*修改*/
    @Test
    public void update(){
        SysRole sysRole = sysRoleMapper.selectById(9);
        sysRole.setRoleName("atguigu测试");
        int rows = sysRoleMapper.updateById(sysRole);
        System.out.println(rows);
    }

    /*BaseEntity
    * @TableLogic //逻辑删除
    @TableField("is_deleted")
    private Integer isDeleted;
    * */
    /*逻辑删除*/
    @Test
    public void deleteLogic(){
        int result =  sysRoleMapper.deleteById(9);
        System.out.println("逻辑删除结果:  "+result);
    }

    /*条件查询*/
    @Test
    public void testQuery1(){
        //创建QueryWrapper对象，调用方法封装条件
        QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_name","atguigu测试");
        //调用mp方法实现查寻操作
        List<SysRole> sysRoles = sysRoleMapper.selectList(queryWrapper);
        System.out.println(sysRoles);
    }
    /*LambdaQueryWrapper*/
    @Test
    public void testLambdaQuery(){
        //LambdaQueryWrapper 调用方法封装条件
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleName,"atguigu测试");
        //
        List<SysRole> list = sysRoleMapper.selectList(wrapper);
        System.out.println(list);
    }
}
