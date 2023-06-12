package com.atguigu.process.service;

import com.atguigu.model.process.ProcessRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 审批记录 服务类
 * </p>
 *
 * @author lhy
 * @since 2023-06-06
 */
public interface OaProcessRecordService extends IService<ProcessRecord> {
    /*记录提交记录 记录流程每个节点操作行为*/
    void record(Long processId,Integer status,String description);

}
