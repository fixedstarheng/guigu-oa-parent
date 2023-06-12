package com.atguigu.process.service;

public interface MessageService {
    /*推送待审批人员   参数 流程ID 用户ID 任务ID */
    void pushPendingMessage(Long processId,Long userId,String taskId);

    void pushProcessedMessage(Long processId, Long userId, Integer status);
}
