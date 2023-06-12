package com.atguigu.process.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.auth.service.SysUserService;
import com.atguigu.model.process.Process;
import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.model.system.SysUser;
import com.atguigu.process.service.MessageService;
import com.atguigu.process.service.OaProcessService;
import com.atguigu.process.service.OaProcessTemplateService;
import com.atguigu.security.custom.LoginUserInfoHelper;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService {
    @Autowired
    private OaProcessService processService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private OaProcessTemplateService processTemplateService;
    @Autowired
    private WxMpService wxMpService;

    /*推送待审批人员   参数 流程ID 用户(推送对象)ID 任务ID */
    @Override
    public void pushPendingMessage(Long processId, Long userId, String taskId) {
        System.out.println("推送待审批人员///——————");
        //get data by id
        Process process = processService.getById(processId);
        //推送对象
        SysUser sysUser = sysUserService.getById(userId);
        //查询审批模板信息
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        //获取 提交审批人 的信息
        SysUser submitSysUser = sysUserService.getById(process.getUserId());
        //获取推送对象的openId
        String openId = sysUser.getOpenId();
           //TODO 这个不符合正式环境流程 仅为了便于自己调试  openId为空 设置openid为自己的openid
        if(StringUtils.isEmpty(openId)){
            openId = "o_jxl6lzXY8p8GjlxdkDaJn5MCKM";
        }
        System.out.println("最后的OpenId: "+ openId);
        //设置消息发送信息
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                //给谁发送消息，openid值
                .toUser(openId)
                //公众平台测试号管理 模板消息接口 接口Id
                .templateId("OgjYpwSyYLs8kxWSuhtddYc020EQMlZcC3dgY7C0f-A")
                //点击消息，跳转的地址
                .url("http://192.168.0.161:9090/#/show/" + processId + "/" + taskId)
                .build();
        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
        JSONObject formShowData = jsonObject.getJSONObject("formShowData");
        StringBuffer content = new StringBuffer();
        for (Map.Entry entry : formShowData.entrySet()) {
            content.append(entry.getKey()).append("：").append(entry.getValue()).append("\n ");
        }
        //设置模板里面参数值
        //{{first.DATA}} 审批编号：{{keyword1.DATA}} 提交时间：{{keyword2.DATA}} {{content.DATA}}
        templateMessage.addData(new WxMpTemplateData("first", submitSysUser.getName()+"提交了"+processTemplate.getName()+"审批申请，请注意查看。", "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword1", process.getProcessCode(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword2", new DateTime(process.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"), "#272727"));
        templateMessage.addData(new WxMpTemplateData("content", content.toString(), "#272727"));
        String msg = null;
        try {
            msg = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
            System.out.println("推送待审批人员 审批申请，请注意查看///————mag："+msg);
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
        log.info("推送消息返回：{}", msg);
    }


    @Override
    public void pushProcessedMessage(Long processId, Long userId, Integer status) {
        Process process = processService.getById(processId);
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        SysUser sysUser = sysUserService.getById(userId);
        SysUser currentSysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());
        String openid = sysUser.getOpenId();
        if(StringUtils.isEmpty(openid)) {
            openid = "o_jxl6lzXY8p8GjlxdkDaJn5MCKM";
        }
        System.out.println("最后的OpenId: "+ openid);
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(openid)//要推送的用户openid
                .templateId("-pgclhU3ddvkSxCZdnwo883Mc3HscwZK7bqheuEnEEU")//模板id
                .url("http://192.168.0.161:9090/#/show/"+processId+"/0")//点击模板消息要访问的网址
                .build();
        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
        JSONObject formShowData = jsonObject.getJSONObject("formShowData");
        StringBuffer content = new StringBuffer();
        for (Map.Entry entry : formShowData.entrySet()) {
            content.append(entry.getKey()).append("：").append(entry.getValue()).append("\n ");
        }
        templateMessage.addData(new WxMpTemplateData("first", "你发起的"+processTemplate.getName()+"审批申请已经被处理了，请注意查看。", "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword1", process.getProcessCode(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword2", new DateTime(process.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword3", currentSysUser.getName(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword4", status == 1 ? "审批通过" : "审批拒绝", status == 1 ? "#009966" : "#FF0033"));
        templateMessage.addData(new WxMpTemplateData("content", content.toString(), "#272727"));
        String msg = null;
        try {
            msg = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
            System.out.println("审批申请已经被处理了，请注意查看///————mag："+msg);
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
        log.info("推送消息返回：{}", msg);
    }

}
