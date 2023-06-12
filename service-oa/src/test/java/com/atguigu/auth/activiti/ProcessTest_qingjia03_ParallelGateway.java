package com.atguigu.auth.activiti;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
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

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProcessTest_qingjia03_ParallelGateway {
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

    /**1.流程定义部署——方法：单个文件部署 */
    @Test
    public void deployProcess(){
        //流程部署
        Deployment deploy = repositoryService.createDeployment()
                //加载资源路径 路径一般是target下的路径
                .addClasspathResource("process/qingjia03ParallelGateway.bpmn20.xml")
                .addClasspathResource("process/qingjia03ParallelGateway.png")
                .name("请假申请流程03-并行网关")
                .deploy();
        System.out.println("流程ID: " + deploy.getId());
        System.out.println("流程名字: " + deploy.getName());
    }
    /**2.启动流程实例  qingjia02Gateway_exclusive*/
    @Test
    public void startProcess(){

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("qingjia03ParallelGateway");
        System.out.println("流程定义ID: " + processInstance.getProcessDefinitionId());
        System.out.println("流程实例ID: " + processInstance.getId());
        System.out.println("当前活动Id: " + processInstance.getActivityId());
    }
    /**3.查询个人待办事务*/
    @Test
    public void findTaskList(){
//        String assign = "wangwu";
//        String assign = "gouwa";
        String assign = "xiaoli";
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
    /**4.办理个人任务*/
    @Test
    public void completTask(){
//        String assign = "wangwu";
                String assign = "xiaoli";
        Task task = taskService.createTaskQuery()
                .taskAssignee(assign)  //要查询的负责人
                .singleResult();//返回一条
        taskService.complete(task.getId());
    }
    /*查询流程定义*/
    @Test
    public void findProcessDefinitionList(){
        List<ProcessDefinition> definitionList = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("qingjia03ParallelGateway")
                .orderByProcessDefinitionVersion()
                .desc()
                .list();
        //输出流程定义信息
        for (ProcessDefinition processDefinition : definitionList) {
            System.out.println("流程定义 id="+processDefinition.getId());
            System.out.println("流程定义 name="+processDefinition.getName());
            System.out.println("流程定义 key="+processDefinition.getKey());
            System.out.println("流程定义 Version="+processDefinition.getVersion());
            System.out.println("流程部署ID ="+processDefinition.getDeploymentId());
        }
    }

    /**删除流程定义
     * 当流程定义已有流程实例启动,即有流程未审批结束时,删除流程定义则失败
     *      * 可以使用repositoryService.deleteDeployment("1",true)进行强制删除
     *      * true:级联删除,会先删除没有完成的流程结点，最后删除流程定义信息
     *      * false:不级联,不会删除没有完成的流程结点,直接删除流程定义信息
     *
     * */
    @Test
    public void deleteDeployment() {
        //部署id
        String deploymentId = "5a5b56d3-010a-11ee-8061-f430b99350b4";
        //删除流程定义，如果该流程定义已有流程实例启动则删除时出错
        repositoryService.deleteDeployment(deploymentId);
        //设置true 级联删除流程定义，即使该流程有流程实例启动也可以删除，设置为false非级别删除方式
        //repositoryService.deleteDeployment(deploymentId, true);
    }
}
