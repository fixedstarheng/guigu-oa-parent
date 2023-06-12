package com.atguigu.auth.activiti;

import org.springframework.stereotype.Component;
/**
 userBean 是 spring 容器中的一个 bean，表示调用该 bean 的 getUsername(int id)方法。

 经理审批：${userBean.getUsername(1)}

 人事审批：${userBean.getUsername(2)}

 *   */
@Component
public class UserBean {
    public String getUsername(int id){
        if(id==1){
            return "zhangsan";
        }
        if((id == 2)){
            return "lisi";
        }
        return "admin";
    }
}
