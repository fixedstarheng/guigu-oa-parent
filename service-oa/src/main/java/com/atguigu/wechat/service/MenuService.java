package com.atguigu.wechat.service;


import com.atguigu.model.wechat.Menu;
import com.atguigu.vo.wechat.MenuVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 菜单 服务类
 * </p>
 *
 * @author lhy
 * @since 2023-06-07
 */
public interface MenuService extends IService<Menu> {

    /*获取全部菜单  （树形结构）*/
    List<MenuVo> findMenuInfo();
    /*同步菜单 接口*/
    public void syncMenu();
    /*删除同步菜单*/
    void removeMenu();
}
