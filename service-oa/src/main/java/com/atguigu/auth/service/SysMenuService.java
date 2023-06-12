package com.atguigu.auth.service;

import com.atguigu.model.system.SysMenu ;
import com.atguigu.vo.system.AssginMenuVo;
import com.atguigu.vo.system.RouterVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 菜单表 服务类
 * </p>
 *
 * @author lhy
 * @since 2023-05-29
 */
public interface SysMenuService extends IService<SysMenu> {
    /*获取菜单列表   菜单树形数据     */
    public List<SysMenu> findNodes();
    /*删除菜单树节点 确保子节点也被删除*/
    public void removeMenuById(Long id);
    /**
     * 根据角色获取授权权限数据
     * @return
     */
    List<SysMenu> findMenuByRoleId(Long roleId);

    /**
     * 保存角色权限
     * @param  assginMenuVo
     */
    void doAssign(AssginMenuVo assginMenuVo);
    /**
     * 根据用户id获取用户可以操作的  菜单  列表
     * */
    List<RouterVo> findUserMenuListByUserId(Long userId);
    /**
     *根据用户id获取用户可以操作  按钮  列表
     * */
    List<String> findUserPermsByUserId(Long userId);
}
