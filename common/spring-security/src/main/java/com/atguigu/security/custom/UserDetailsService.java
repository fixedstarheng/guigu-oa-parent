package com.atguigu.security.custom;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
/**自定义3组件：第2.loadUserByUsername: 获取用户信息
 * UserDetailsService 接口（interface）
 * 这个接口的实现类 在service-oa/service/impl 里
 * */
//业务对象
public interface UserDetailsService extends org.springframework.security.core.userdetails.UserDetailsService {
    /**
     * 根据用户名获取用户对象（获取不到直接抛异常）
     */
    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
