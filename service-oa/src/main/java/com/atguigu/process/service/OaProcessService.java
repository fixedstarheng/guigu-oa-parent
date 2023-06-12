package com.atguigu.process.service;

import com.atguigu.model.process.Process;
import com.atguigu.vo.process.ApprovalVo;
import com.atguigu.vo.process.ProcessFormVo;
import com.atguigu.vo.process.ProcessQueryVo;
import com.atguigu.vo.process.ProcessVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.Map;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author lhy
 * @since 2023-06-03
 */
public interface OaProcessService extends IService<Process> {
    /*分页*/
    IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo);

    /*流程定义部署*/
    void deployByZip(String deployPath);

    /*启动流程实例*/
    void startUp(ProcessFormVo processFormVo);

    /*查询待处理任务列表*/
    IPage<ProcessVo> findPending(Page<Process> pageParam);

    /*查看审批详情*/
    Map<String, Object> show(Long id);

    /*审批*/
    void approve(ApprovalVo approvalVo);

    /*查看已处理审批*/
    IPage<ProcessVo> findProcessed(Page<Process> pageParam);

    /*查看已发起审批*/
    IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam);
}
