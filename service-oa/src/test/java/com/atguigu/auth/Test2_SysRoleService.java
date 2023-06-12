package com.atguigu.auth;

import com.atguigu.auth.service.SysRoleService;
import com.atguigu.model.system.SysRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class Test2_SysRoleService {
    @Autowired
    SysRoleService sRService;

    /*查询所有记录*/
    @Test
    public void getAll(){
        List<SysRole> list =  sRService.list();
        System.out.println(list);
    }
}
