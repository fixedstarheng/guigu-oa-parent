package com.atguigu.auth.activiti;

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

/**流程变量  变量作用域是整个流程实例*/
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProcessTestVariable {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
/*    *//*删除流程定义*//*
    @Test
    public void deleteDeployment() {
        //部署id
        String deploymentId = "1c272185-0064-11ee-883a-f430b99350b4";
        //删除流程定义，如果该流程定义已有流程实例启动则删除时出错
        repositoryService.deleteDeployment(deploymentId);
        //设置true 级联删除流程定义，即使该流程有流程实例启动也可以删除，设置为false非级别删除方式
        //repositoryService.deleteDeployment(deploymentId, true);
    }*/
    /*查询流程定义*/
    @Test
    public void findProcessDefinitionList(){
        List<ProcessDefinition> definitionList = repositoryService.createProcessDefinitionQuery().processDefinitionName("加班申请流程")
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

    /*流程定义 部署*/
    @Test
    public void deployProcess(){
        //流程部署
        Deployment deploy = repositoryService.createDeployment()
                //加载资源路径 路径一般是target下的路径
                .addClasspathResource("process/jiaban.bpmn20.xml")
                .addClasspathResource("process/jiaban.png")
                .name("加班申请流程")
                .deploy();
        System.out.println("加班流程ID: " + deploy.getId());
        System.out.println("加班流程名字: " + deploy.getName());
    }
    /*启动流程实例*/
    @Test
    public void startProcessInstance(){
 /**这里使用的流程变量为 globa变量 主要表现为 Map<key，value> key为流程变量名，map中可以放置多个变量 */
        /*启动流程实例时 设置变量*/
        Map<String,Object> map = new HashMap<>();
        //设置任务人
        map.put("assignee1","lucy03");
//注释掉是为了在处理任务时 利用流程变量 临时分配assignee2 人  map.put("assignee2","marry02");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("jiaban",map);
        System.out.println("流程定义ID: " + processInstance.getProcessDefinitionId());
        System.out.println("流程实例ID: " + processInstance.getId());
    }
    /*查询个人待办任务*/
    @Test
    public void findTaskList(){
        String assign = "lucy03";
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
    /**4.用户办理任务*//*处理当前任务*/
    @Test
    public void completeTask(){
        //查询负责人需要处理的任务 //一个任务
        Task task = taskService.createTaskQuery().taskAssignee("lucy03")
                .singleResult();
        /**在任务办理的时候 设置变量*/
        Map<String,Object> variables = new HashMap<>();
        //设置流程变量assignee2 为 zhao
        variables.put("assignee2","zhao");
        //处理任务 参数：任务ID
        taskService.complete(task.getId(),variables);//
    }
    /*查询assignee2待办任务*/
    @Test
    public void findTaskList_assignee2(){
        String assign = "zhao";
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
