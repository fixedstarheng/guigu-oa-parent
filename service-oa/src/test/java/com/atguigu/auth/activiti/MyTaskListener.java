package com.atguigu.auth.activiti;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**分配任务 第三种方式：  监听器分配*/
public class MyTaskListener implements TaskListener {
    @Override
    public void notify(DelegateTask task) {
        if(task.getName().equals("经理审批")){
            //分配任务
            task.setAssignee("jack");
        }else if(task.getName().equals("人事审批")){
            task.setAssignee("tom");
        }
    }
}
