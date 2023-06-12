package com.atguigu.auth.activiti;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
/**Activity 流程操作
 * 流程定义： 部署、查询、删除、查看历史
    * 流程实例：  启动、业务标识、挂起/激活（全部 or 单个）
 * */
/*请假流程*/
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProcessTest {
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

    /**创建 流程实例，指定BusinessKey(业务标识)*/
    @Test
    public void startUpProcessAddBusinessKey(){
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("qingjia", "1001");
        // 输出
        System.out.println("业务id:"+processInstance.getBusinessKey());
    }
    /**挂起，激活 流程实例*/
        /*挂起全部 流程实例*/
        @Test
        public void suspendProcessInstanceAll(){
            //1.获取流程定义的对象
            ProcessDefinition qingjia = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey("qingjia").singleResult();
            //2.调用流程定义对象的方法判断当前状态：挂起suspended=true  激活
            boolean suspended = qingjia.isSuspended();
            //3.判断如果挂起(suspended=true)，实现激活
            if(suspended){
              //activateProcessDefinitionById() 三个参数
                //第一个参数 流程定义id
                //第二个参数 是否激活 true
                //第三个参数 时间点
                repositoryService
                        .activateProcessDefinitionById(qingjia.getId(),true,null);
                System.out.println(qingjia.getId()+"激活了");
            }else{//如果激活，实现挂起
              //suspendProcessDefinitionById() 三个参数
                //第一个参数 流程定义id
                //第二个参数 是否挂起 true
                //第三个参数 时间点
                repositoryService.suspendProcessDefinitionById(qingjia.getId(),true,null);
                System.out.println(qingjia.getId()+"挂起");
            }

        }
        /*挂起单个 流程实例*/
        @Test
        public void SingleSuspendProcessInstance() {
            String processInstanceId = "55c31a10-002a-11ee-9d6a-f430b99350b4";
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            //获取到当前流程定义是否为暂停状态   suspended方法为true代表为暂停   false就是运行的
            boolean suspended = processInstance.isSuspended();
            if (suspended) {
                runtimeService.activateProcessInstanceById(processInstanceId);
                System.out.println("流程实例:" + processInstanceId + "激活");
            } else {
                runtimeService.suspendProcessInstanceById(processInstanceId);
                System.out.println("流程实例:" + processInstanceId + "挂起");
            }
        }

    /**1.流程定义部署——部署自建的请假流程模型 *//* 方法：单个文件部署 */
    @Test
    public void deployProcess(){
        //流程部署
        Deployment deploy = repositoryService.createDeployment()
                //加载资源路径 路径一般是target下的路径
                .addClasspathResource("process/qingjia.bpmn20.xml")
                .addClasspathResource("process/qingjia.png")
                .name("请假申请流程")
                .deploy();
        System.out.println("流程ID: " + deploy.getId());
        System.out.println("流程名字: " + deploy.getName());
    }

    /**2.启动流程实例*/
    @Test
    public void startProcess(){
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("qingjia");
        System.out.println("流程定义ID: " + processInstance.getProcessDefinitionId());
        System.out.println("流程实例ID: " + processInstance.getId());
        System.out.println("当前活动Id: " + processInstance.getActivityId());
    }

    /**3.查询任务*//*查询当前个人待执行的任务*/
    @Test
    public void findTaskList(){
        String assign = "lisi";
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
        Task task = taskService.createTaskQuery().taskAssignee("zhangsan")
                .singleResult();
        //处理任务 参数：任务ID
        taskService.complete(task.getId());//
    }


    /**5.查询已处理任务list*//**/
    @Test
    public void findcompleteTaskList(){
        List<HistoricTaskInstance> historicTaskInstanceList = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee("zhangsan")
                .finished().list();
        if(historicTaskInstanceList.isEmpty()){
            System.out.println("没有已处理任务列表");
        }
        for (HistoricTaskInstance historicTaskInstance : historicTaskInstanceList) {
            System.out.println("流程实例id：" + historicTaskInstance.getProcessInstanceId());
            System.out.println("任务id：" + historicTaskInstance.getId());
            System.out.println("任务负责人：" + historicTaskInstance.getAssignee());
            System.out.println("任务名称：" + historicTaskInstance.getName());
        }
    }

    /*查询流程定义*/
    @Test
    public void findProcessDefinitionList(){
        List<ProcessDefinition> definitionList = repositoryService.createProcessDefinitionQuery()
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
        String deploymentId = "2bbb3e6b-0027-11ee-a52d-f430b99350b4";
        //删除流程定义，如果该流程定义已有流程实例启动则删除时出错
        repositoryService.deleteDeployment(deploymentId);
        //设置true 级联删除流程定义，即使该流程有流程实例启动也可以删除，设置为false非级别删除方式
        //repositoryService.deleteDeployment(deploymentId, true);
    }
}
