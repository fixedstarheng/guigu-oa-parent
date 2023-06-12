package com.atguigu.process.controller;


import com.atguigu.common.result.Result;
import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.model.process.ProcessType;
import com.atguigu.process.service.OaProcessTemplateService;
import com.atguigu.security.custom.LoginUserInfoHelper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 审批模板 前端控制器
 * </p>
 *
 * @author lhy
 * @since 2023-06-02
 */
@Api( tags = "审批模板管理")//value = "审批模板管理",
@RestController
@RequestMapping(value = "/admin/process/processTemplate")
@CrossOrigin //跨域
public class OaProcessTemplateController {
    @Autowired
    private OaProcessTemplateService processTemplateService;

/*分页查询审批模板*/
    @ApiOperation(value = "获取分页查询审批模板数据")
    @GetMapping("{page}/{limit}")
    public Result index(@PathVariable Long page, @PathVariable Long limit){
        System.out.println("////////////////admin/process/processTemplate/1/10//////////////////");
        Page<ProcessTemplate> pageParam = new  Page<>(page,limit);
        //分页查询审批模板，因为审批模板里包含有审批类型id 需要把审批类型对应名称查询
        IPage<ProcessTemplate> pageModel = processTemplateService.selectPageProcessTempate(pageParam);
        return Result.ok(pageModel);
    }
    //@PreAuthorize("hasAuthority('bnt.processTemplate.list')")
    @ApiOperation(value = "获取")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        ProcessTemplate processTemplate = processTemplateService.getById(id);
        return Result.ok(processTemplate);
    }

    //@PreAuthorize("hasAuthority('bnt.processTemplate.templateSet')")
    @ApiOperation(value = "新增")
    @PostMapping("save")
    public Result save(@RequestBody ProcessTemplate processTemplate) {
        processTemplateService.save(processTemplate);
        return Result.ok();
    }

    //@PreAuthorize("hasAuthority('bnt.processTemplate.templateSet')")
    @ApiOperation(value = "修改")
    @PutMapping("update")
    public Result updateById(@RequestBody ProcessTemplate processTemplate) {
        processTemplateService.updateById(processTemplate);
        return Result.ok();
    }

    //@PreAuthorize("hasAuthority('bnt.processTemplate.remove')")
    @ApiOperation(value = "删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        processTemplateService.removeById(id);
        return Result.ok();
    }
/**上传 流程定义 文件 到 后端 classpath固定文件夹*/
    @ApiOperation(value = "上传流程定义")
    @PostMapping("/uploadProcessDefinition")
    public Result uploadProcessDefinition(MultipartFile file) throws FileNotFoundException {
        //获取classes目录位置
        String path = new File(ResourceUtils.getURL("classpath:").getPath()).getAbsolutePath();
        //设置上传文件夹
         //上传目录
        File tempFile = new File(path+"/processes/");
        // 判断目录是否存在
        if(!tempFile.exists()){
            tempFile.mkdirs();//创建目录
        }
//        if(){}
        String filename = file.getOriginalFilename();
        //创建空文件(用于写入文件)，实现文件写入
        File zipFile = new File(path + "/processes/" + filename);
        //保存文件
        try {
            file.transferTo(zipFile);
        } catch (IOException e) {
            return Result.fail("上传失败");
        }
        Map<String,Object> map = new HashMap<>();
        //根据上传地址后续部署流程定义，文件名称为流程定义的默认key
        map.put("processDefinitionPath","processes/"+filename);
        map.put("processDefinitionKey",filename.substring(0,filename.lastIndexOf(".")));
        return Result.ok(map);
    }
/**发布（部署 流程定义，不是创建流程实例）*/
    @ApiOperation(value = "发布")
    @GetMapping("/publish/{id}")
    public Result  publish(@PathVariable Long id) {
        //测试当前上传对象为
        String uname = LoginUserInfoHelper.getUsername();
        System.out.println("流程定义 部署 publish///____username"+uname);

        //修改模板发布状态 1 已经发布
        processTemplateService.publish(id);
        return Result.ok();
    }
    //测试
    public static void main(String[] args) {
        try {
            String path = new File(ResourceUtils.getURL("classpath:").getPath()).getAbsolutePath();
            System.out.println(path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

