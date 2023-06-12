package com.atguigu.process.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.auth.service.SysUserService;
import com.atguigu.model.process.Process;
import com.atguigu.model.process.ProcessRecord;
import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.model.system.SysUser;
import com.atguigu.process.mapper.OaProcessMapper;
import com.atguigu.process.service.MessageService;
import com.atguigu.process.service.OaProcessRecordService;
import com.atguigu.process.service.OaProcessService;
import com.atguigu.process.service.OaProcessTemplateService;
import com.atguigu.security.custom.LoginUserInfoHelper;
import com.atguigu.vo.process.ApprovalVo;
import com.atguigu.vo.process.ProcessFormVo;
import com.atguigu.vo.process.ProcessQueryVo;
import com.atguigu.vo.process.ProcessVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;


/**审批管理 列表
 *
 * @author lhy
 * @since 2023-06-03
 */
@Service
public class OaProcessServiceImpl extends ServiceImpl<OaProcessMapper, Process> implements OaProcessService {
   @Autowired
   private RepositoryService repositoryService;
   @Autowired
   private OaProcessTemplateService processTemplateService;
   @Autowired
   private SysUserService sysUserService;
   @Autowired
   private RuntimeService runtimeService;
   @Autowired
   private TaskService taskService;
   @Autowired
   private OaProcessRecordService processRecordService;
   @Autowired
   private HistoryService historyService;

    /*分页*/
    @Override
    public IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo) {
        IPage<ProcessVo> pageModel = baseMapper.selectPage(pageParam,processQueryVo);
        return pageModel;
    }

    /*流程定义部署*/
    @Override
    public void deployByZip(String deployPath) {
        System.out.println("====================OaProcessServiceImpl.deployByZip=============================="+deployPath);
        // 定义zip输入流
        InputStream inputStream = this
                .getClass()
                .getClassLoader()
                .getResourceAsStream(deployPath);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
         // 流程部署
        Deployment deployment = repositoryService.createDeployment()
                .addZipInputStream(zipInputStream)
                .addClasspathResource(deployPath)
                .deploy();

    }

    /*推送消息*/
    @Autowired
    private MessageService messageService;

/**启动流程实例 */
    @Override
    public void startUp(ProcessFormVo processFormVo) {
        //1 根据当前用户id获取用户信息
        //2 根据审批模板id把模板信息查询
        //3 保存提交审批信息到业务表（oa_process）记录信息
        //4 启动流程实例 RuntimeService
           //4.1 流程定义key //4.2 业务key processId //4.3 流程参数 form表单json数据，转换map集合
        //5 查询下一个审批人
        //6 推送消息
        //7 业务和流程关联
  /**Code Start*/
        //1用户信息 根据当前用户id获取用户信息
//            sout启动流程实例 打印username
        System.out.println("启动流程实例///LoginUserInfoHelper————uname: "+LoginUserInfoHelper.getUsername());
            SysUser sysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());
        //2模板信息 根据审批模板id把模板信息查询
            ProcessTemplate processTemplate = processTemplateService.getById(processFormVo.getProcessTemplateId());
        //3提交的审批信息 保存提交审批信息到业务表（oa_process）记录信息
            Process process = new Process();
            //processFormVo复制到process对象里面
            BeanUtils.copyProperties(processFormVo,process);
            //其他值
            process.setStatus(1);
            String workNo = System.currentTimeMillis()+"";
            process.setProcessCode(workNo);
            process.setUserId(LoginUserInfoHelper.getUserId());
            process.setFormValues(processFormVo.getFormValues());
            process.setTitle(sysUser.getName()+"发起"+processTemplate.getName()+"申请");
            baseMapper.insert(process);
        //4 启动流程实例 RuntimeService
            //4.1 流程定义key
            String processDefinitionKey = processTemplate.getProcessDefinitionKey();
            //4.2 业务key processId
            String businessKey = String.valueOf(process.getId());
            //4.3 流程参数 form表单json数据，转换map集合
            String formValues = processFormVo.getFormValues();
              //名称是固定的formData
                JSONObject jsonObject = JSON.parseObject(formValues);
                JSONObject formData = jsonObject.getJSONObject("formData");
              //遍历formData得到内容，封装map集合
            Map<String,Object> map = new HashMap<>();
            for (Map.Entry<String, Object> entry : formData.entrySet()) {
                map.put(entry.getKey(),entry.getValue());
            }
            Map<String,Object> variables = new HashMap<>();
            variables.put("data",map);
            //启动流程实例
        System.out.println("////processDefinitionKey: "+processDefinitionKey);
        String key = processDefinitionKey.substring(0,processDefinitionKey.lastIndexOf(".bpmn20"));
        System.out.println("//// Key: "+key);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key, businessKey, variables);
        //5 查询下一个审批人
          //审批人可能多个
          List<Task> taskList =  this.getCurrentTaskList(processInstance.getId());
          List<String> nameList = new ArrayList<>();
        for (Task task : taskList) {
            String assigneeName = task.getAssignee();
            //得到用户信息
            SysUser user = sysUserService.getByUsername(assigneeName);
            String name = user.getName();
            nameList.add(name);

            //TODO 6 推送消息
            messageService.pushPendingMessage(process.getId(),user.getId(), task.getId());

        }
        process.setProcessInstanceId(processInstance.getId());
        process.setDescription("等待"+ StringUtils.join(nameList.toArray(),",") +"审批");
        //7 业务和流程关联  更新oa_process数据 完善信息
        baseMapper.updateById(process);
         //记录操作审批数据 Long processId, Integer status, String description
         processRecordService.record(process.getId(),1,"发起申请");
 /**Code END*/
    }

    /*获取当前任务list*/
    private List<Task> getCurrentTaskList(String id){
        List<Task> list = taskService.createTaskQuery().processInstanceId(id).list();
        return list;
    }

    /*查询待处理 任务列表*/
        /*
        1 封装查询条件，根据当前登录的用户名称
        2.调用方法分页条件查询 返回 待办任务 列表
        3.封装返回list集合数据 到List<ProcessVo>里面
        4.封装返回Ipage对象
         */
    @Override
    public IPage<ProcessVo> findPending(Page<Process> pageParam) {
        String uname = LoginUserInfoHelper.getUsername();
        System.out.println("findPending///____username"+uname);
        TaskQuery query = taskService.createTaskQuery()
                .taskAssignee(uname)
                .orderByTaskCreateTime()
                .desc();
        //listPage 2个参数 开始位置 每页显示记录数
           //开始位置  分页记录条开始位置 （当前要展示页-1）*size = 要展示开始记录位置
        int begin = (int) ((pageParam.getCurrent()-1)*pageParam.getSize());
        int size = (int) pageParam.getSize();
        List<Task> taskList = query.listPage(begin,size);
        long totalCount = query.count();
        //将List<Task>待办任务集合 --封装到--> List<ProcessVo>
        List<ProcessVo> processVoList = new ArrayList<>();
        for (Task task : taskList) {
           //从task获取流程实例id
            String processInstanceId = task.getProcessInstanceId();
            System.out.println("getProcessInstanceId///_____"+processInstanceId);
           //根据流程实例id获取实例对象
            ProcessInstance processInstance =
                            runtimeService.createProcessInstanceQuery()
                                          .processInstanceId(processInstanceId)
                                          .singleResult();
            //从流程实例对象获取业务key--当时存的processId
            String businessKey = processInstance.getBusinessKey();
            if(businessKey == null){
                continue;
            }
           //根据业务key获取Process对象
            long processId = Long.parseLong(businessKey);
            System.out.println("businessKey.long///____"+processId);
            Process process = baseMapper.selectById(processId);
           //Process对象 复制 ProcessVo对象
            ProcessVo processVo = new ProcessVo();
            System.out.println("process///____"+process.toString());
            BeanUtils.copyProperties(process,processVo);
            processVo.setTaskId(task.getId());
            //放到最终list集合processVoList
            processVoList.add(processVo);
            //封装返回IPage对象
            IPage<ProcessVo> page = new Page<ProcessVo>(pageParam.getCurrent(),pageParam.getSize(),totalCount);
            page.setRecords(processVoList);
            return page;
        }
        return null;
    }

    /*查看审批详情*/
        /*
            1.根据流程id获取流程信息Process
            2.根据流程id获取流程记录信息
            3.根据模板id查询模板信息
            4.判断当前用户是否可以审批
               可以看到信息不一定能审批，不能重复审批
            5.查询数据封装到map集合，返回
        */
    @Override
    public Map<String, Object> show(Long id) {
        Process process = this.getById(id);
        List<ProcessRecord> processRecordList = processRecordService.list(new LambdaQueryWrapper<ProcessRecord>().eq(ProcessRecord::getProcessId, id));
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        Map<String, Object> map = new HashMap<>();
        map.put("process", process);
        map.put("processRecordList", processRecordList);
        map.put("processTemplate", processTemplate);
        //计算当前用户是否可以审批，能够查看详情的用户不是都能审批，审批后也不能重复审批
        boolean isApprove = false;
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        if (!CollectionUtils.isEmpty(taskList)) {
            for(Task task : taskList) {
                if(task.getAssignee().equals(LoginUserInfoHelper.getUsername())) {
                    isApprove = true;
                }
            }
        }
        map.put("isApprove", isApprove);
        return map;
    }

    /*进行审批操作*/
    @Override
    public void approve(ApprovalVo approvalVo) {
        //1 从approvalVo获取任务id，根据任务id获取流程变量
        String taskId = approvalVo.getTaskId();
        Map<String,Object> variables = taskService.getVariables(taskId);
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
        //2 判断审批状态值
           //2.1 状态值 = 1 审批通过
           //2.2 状态值 =-1 驳回审批 流程直接结束
        if(approvalVo.getStatus() == 1){
            Map<String,Object> variable = new HashMap<>();
            taskService.complete(taskId,variable);
        } else if (approvalVo.getStatus() == -1) {
            //驳回 结束审批
           this.endTask(taskId);
        }
        //3 记录审批相关过程信息 oa_process_record表
        //Long processId,Integer status,String description
        String description = approvalVo.getStatus().intValue() == 1?"已通过":"驳回";
        processRecordService.record(approvalVo.getProcessId(),approvalVo.getStatus(),description);
        //4 查询下一个审批人 更新流程表记录 process表记录
        Process process = baseMapper.selectById(approvalVo.getProcessId());
        //查询任务
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        if(!CollectionUtils.isEmpty(taskList)){
            List<String> assignList = new ArrayList<>();
            for(Task task : taskList){
                String assignee = task.getAssignee();
                SysUser sysUser = sysUserService.getByUsername(assignee);
                assignList.add(sysUser.getName());
                //TODO 公众号消息推送消息给下一个审批人

            }
            //更新 process流程
            process.setDescription("等待" + StringUtils.join(assignList.toArray(), ",") + "审批");
            process.setStatus(1);
        }else{//没有下一个审批人 oa_process
            if(approvalVo.getStatus().intValue() == 1) {
                process.setDescription("审批完成（同意）");
                process.setStatus(2);//status=2 流程结束
            } else {
                process.setDescription("审批完成（拒绝）");
                process.setStatus(-1);
            }
        }
        baseMapper.updateById(process);
    }

    /*查看已处理审批*/
     /*
        1.封装查询条件
        2.调用方法条件分页查询，返回list集合
        3.遍历返回list集合，封装list<ProcessVo>
        4.IPage封装分页查询所有数据，返回
      */
    @Override
    public IPage<ProcessVo> findProcessed(Page<Process> pageParam) {
        // 根据当前人的ID查询
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery().taskAssignee(LoginUserInfoHelper.getUsername()).finished().orderByTaskCreateTime().desc();
        List<HistoricTaskInstance> list = query.listPage((int) ((pageParam.getCurrent() - 1) * pageParam.getSize()), (int) pageParam.getSize());
        long totalCount = query.count();

        List<ProcessVo> processList = new ArrayList<>();
        for (HistoricTaskInstance item : list) {
            String processInstanceId = item.getProcessInstanceId();
            Process process = this.getOne(new LambdaQueryWrapper<Process>().eq(Process::getProcessInstanceId, processInstanceId));
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process, processVo);
            processVo.setTaskId("0");
            processList.add(processVo);
        }
        IPage<ProcessVo> page = new Page<ProcessVo>(pageParam.getCurrent(), pageParam.getSize(), totalCount);
        page.setRecords(processList);
        return page;
    }


    /*结束流程 被驳回(拒绝)后调用*/
        /*
            1.根据任务id获取任务对象Task
            2.获取流程定义模型 BpmnModel
            3.获取结束流向节点
            4.当前流向节点
            5.清理当前流动方向
            6.创建新流向
            7.当前节点指向新方向
            8.完成当前任务
         */
    private void endTask(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        //流程定义模型BpmnModel
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        List endEventList =  bpmnModel.getMainProcess().findFlowElementsOfType(EndEvent.class);
        //并行任务可能为null //理解：比如只需通过一个人审批 而这个人刚好拒绝 就没有并行任务了
        if(CollectionUtils.isEmpty(endEventList)) {
            return;
        }
        //结束流向节点endFlowNode
        FlowNode endFlowNode = (FlowNode) endEventList.get(0);
        //当前流向节点
        FlowNode currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());

        //  临时保存当前活动的原始方向
        List originalSequenceFlowList = new ArrayList<>();
        originalSequenceFlowList.addAll(currentFlowNode.getOutgoingFlows());

        //清理节点
        currentFlowNode.getOutgoingFlows().clear();

        //新流向 建立
        SequenceFlow newSequenceFlow = new SequenceFlow();
        newSequenceFlow.setId("newSequenceFlowId");
        newSequenceFlow.setSourceFlowElement(currentFlowNode);
        newSequenceFlow.setTargetFlowElement(endFlowNode);

        //  当前节点指向新的方向
        List newSequenceFlowList = new ArrayList<>();
        newSequenceFlowList.add(newSequenceFlow);
        currentFlowNode.setOutgoingFlows(newSequenceFlowList);

        //  完成当前任务
        taskService.complete(task.getId());

    }

    /*查看已发起审批*/
    @Override
    public IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam) {
        ProcessQueryVo processQueryVo = new ProcessQueryVo();
        processQueryVo.setUserId(LoginUserInfoHelper.getUserId());
        IPage<ProcessVo> page = baseMapper.selectPage(pageParam, processQueryVo);
        for (ProcessVo item : page.getRecords()) {
            item.setTaskId("0");
        }
        return page;
    }
}
