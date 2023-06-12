package com.atguigu.auth.activiti;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**任务分配 表达式分配 uel-method*/
/*加班流程*/
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProcessTest_jiaban2 {
    //RepositoryService:
    // Activiti 的资源管理类，该服务负责部署流程定义，管理流程资源
    @Autowired
    private RepositoryService repositoryService;
    //RuntimeService:
    //Activiti 的流程运行管理类，用于开始一个新的流程实例，获取关于流程执行的相关信息。
    @Autowired
    private RuntimeService runtimeService;
    //TaskService:
    //Activiti 的任务管理类，用于处理业务运行中的各种任务
    @Autowired
    private TaskService taskService;
    //HistoryService
    //Activiti 的历史管理类，可以查询历史信息。
    @Autowired
    private HistoryService historyService;


    /*流程定义 部署*/
    @Test
    public void deployProcess(){
        //流程部署
        Deployment deploy = repositoryService.createDeployment()
                //加载资源路径 路径一般是target下的路径
                .addClasspathResource("process/jiaban2.bpmn20.xml")
                .addClasspathResource("process/jiaban2.png")
                .name("加班申请流程")
                .deploy();
        System.out.println("加班流程ID: " + deploy.getId());
        System.out.println("加班流程名字: " + deploy.getName());
    }
    /*启动流程实例*/
    @Test
    public void startProcessInstance(){
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("jiaban2");
        System.out.println("流程定义ID: " + processInstance.getProcessDefinitionId());
        System.out.println("流程实例ID: " + processInstance.getId());
    }
    /*查询个人待办任务*/
    @Test
    public void findTaskList(){
        String assign = "zhangsan";
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(assign).list();
        if(list.isEmpty()){
            System.out.println("当前个人待执行的任务列表为0");
        }
        for (Task task : list) {
            System.out.println("流程实例ID : "+task.getProcessInstanceId());
            System.out.println("任务id：" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }
}
