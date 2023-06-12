package com.atguigu.auth.controller;

import com.atguigu.auth.service.SysRoleService;
import com.atguigu.common.result.Result;
import com.atguigu.common.config.exception.GuiguException;
import com.atguigu.model.system.SysRole;
import com.atguigu.vo.system.AssginRoleVo;
import com.atguigu.vo.system.SysRoleQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags ="角色管理接口")
@RestController
@RequestMapping("/admin/system/sysRole")
public class SysRoleController {
    //注入service
    @Autowired
    private SysRoleService sysRoleService;

    //根据用户获取角色数据
    @ApiOperation(value = "根据用户获取角色数据")
    @GetMapping("/toAssign/{userId}")
    public Result toAssign(@PathVariable Long userId) {
        Map<String, Object> roleMap = sysRoleService.findRoleByAdminId(userId);
        return Result.ok(roleMap);
    }
    //根据用户分配角色
    @ApiOperation(value = "根据用户分配角色")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestBody AssginRoleVo assginRoleVo) {
        sysRoleService.doAssign(assginRoleVo);
        return Result.ok();
    }


   /* //查询所有角色
    @GetMapping("/findAll")
    public List<SysRole> findAll(){
        List<SysRole> list = sysRoleService.list();
        return list;
    }*/

    //查询所有角色
    //统一返回数据结果
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation("查询所有角色")
    @GetMapping("/findAll")
    public Result findAll(){
        List<SysRole> list = sysRoleService.list();
        return Result.ok(list);
    }

/**条件分页查询
 *
 * */
    //page 当前页 limit 每页显示记录数
    //@PreAuthorize标签控制controller层接口权限
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation("条件分页查询")
    @GetMapping("{page}/{limit}")
    public Result pageQueryRole(@PathVariable Long page, @PathVariable Long limit, SysRoleQueryVo sysRoleQueryVo){
        //调用Service的方法实现
        //1 创建Page对象 传递分页相关参数
        Page<SysRole> pageParam = new Page<>(page,limit);
        //2 封装条件，判断条件是否为空，不为空进行封装
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        String roleName = sysRoleQueryVo.getRoleName();
        if(!StringUtils.isEmpty(roleName)){
            //封装
            wrapper.like(SysRole::getRoleName,roleName);
        }
        //3 调用方法实现
        IPage<SysRole> page1 = sysRoleService.page(pageParam, wrapper);

        return Result.ok(page1);
    }

    /*添加角色*/
    @PreAuthorize("hasAuthority('bnt.sysRole.add')")
    @ApiOperation("添加角色")
    @PostMapping("save")
    public Result save(@RequestBody SysRole role){//@RequestBody 请求体 json形式数据
        //调用service的方法
        Boolean is_success = sysRoleService.save(role);
        if(is_success){
            return Result.ok();
        }else{
            return Result.fail();
        }
    }
    //查询角色--根据id查询
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation("根据id查询")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id){
        SysRole sysRole = sysRoleService.getById(id);
        return Result.ok(sysRole);
    }

    //修改角色
    @PreAuthorize("hasAuthority('bnt.sysRole.update')")
    @ApiOperation("修改角色-byID")
    @PutMapping("update")
    public Result update(@RequestBody SysRole role){
        //调用service方法
        boolean update = sysRoleService.updateById(role);
        if(update){
            return Result.ok();
        }else{
            return Result.fail();
        }
    }

    //删除角色
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation("删除角色——byID")
    @DeleteMapping("delete/{id}")
    public Result delete(@PathVariable Long id){
        //调用service方法
        boolean b = sysRoleService.removeById(id);
        if(b){
            return Result.ok();
        }else{
            return  Result.fail();
        }
    }

    //批量删除角色
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation("批量删除角色")
    @DeleteMapping("batchDelete")
    public Result deleteList(@RequestBody List<Long> ids){
        //调用service方法
        boolean b = sysRoleService.removeByIds(ids);
        if(b){
            return Result.ok();
        }else{
            return  Result.fail();
        }
    }


    //查询所有角色
    //统一返回数据结果
    @ApiOperation("测试全局异常处理")
    @GetMapping("/testException")
    public Result test_Exception(){
        List<SysRole> list = sysRoleService.list();
        //抛出自定义异常
        try {
            //制造模拟异常
            int a = 10/0;
        }catch(Exception e) {
            throw new GuiguException(20001,"出现自定义异常");
        }
        return Result.ok(list);
    }

}
