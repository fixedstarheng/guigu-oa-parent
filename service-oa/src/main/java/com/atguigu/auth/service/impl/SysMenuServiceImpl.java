package com.atguigu.auth.service.impl;

import com.atguigu.auth.mapper.SysRoleMenuMapper;
import com.atguigu.auth.service.SysRoleMenuService;
import com.atguigu.auth.utils.MenuHelper;
import com.atguigu.common.config.exception.GuiguException;
import com.atguigu.model.system.SysMenu ;
import com.atguigu.auth.mapper.SysMenuMapper;
import com.atguigu.auth.service.SysMenuService;
import com.atguigu.model.system.SysRoleMenu;
import com.atguigu.vo.system.AssginMenuVo;
import com.atguigu.vo.system.MetaVo;
import com.atguigu.vo.system.RouterVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author lhy
 * @since 2023-05-29
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {
    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    /*获取菜单列表    菜单树形数据    */
    @Override
    public List<SysMenu> findNodes() {

        //1.查询所有菜单数据
        List<SysMenu> sysMenusList = baseMapper.selectList(null);

        //2.构建出 树形结构  递归
        List<SysMenu> resultList = MenuHelper.buildTree(sysMenusList);
        return resultList;
    }
    /*删除菜单树节点 确保子节点也被删除*/
    @Override
    public void removeMenuById(Long id) {
        //是否拥有子节点
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId,id);
        Integer count = baseMapper.selectCount(wrapper);

        if(count>0){
            throw new GuiguException(201,"菜单不能删除");
        }
        baseMapper.deleteById(id);
    }
    /* 根据角色获取授权权限数据*/
    @Override
    public List<SysMenu> findMenuByRoleId(Long roleId) {
        //1.查询所有菜单————添加条件 status=1
        LambdaQueryWrapper<SysMenu> wrapperSysMenu = new LambdaQueryWrapper<>();
        wrapperSysMenu.eq(SysMenu::getStatus,1);
        List<SysMenu> allSysMenuList = baseMapper.selectList(wrapperSysMenu);
        //2.根据角色id roleId查询 角色菜单关系表里面 角色id对应所有的菜单id
        LambdaQueryWrapper<SysRoleMenu> wrapperSysRoleMenu = new LambdaQueryWrapper<>();
        wrapperSysRoleMenu.eq(SysRoleMenu::getRoleId,roleId);
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuService.list(wrapperSysRoleMenu);
        //3.根据获取菜单id，获取对应菜单对象
        List<Long> menuIdList = sysRoleMenuList.stream().map(e->e.getMenuId()).collect(Collectors.toList());
        //3.1 拿着菜单id 和所有菜单集合里面id进行比较，如果相同封装
        allSysMenuList.forEach(permission -> {
            if (menuIdList.contains(permission.getId())) {
                permission.setSelect(true);
            } else {
                permission.setSelect(false);
            }
        });


        //4.返回规定格式菜单列表
        List<SysMenu> sysMenuList = MenuHelper.buildTree(allSysMenuList);
        return sysMenuList;
    }
    /* 保存角色权限  为角色分配菜单*/
    @Override
    public void doAssign(AssginMenuVo assginMenuVo) {
        //1.根据角色id 删除菜单角色表 分配数据
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId,assginMenuVo.getRoleId());
        sysRoleMenuService.remove(wrapper);
        //2.从参数里面获取角色新分配菜单id列表
        //进行遍历，把每个id数据添加菜单角色表
        List<Long> menuIdList = assginMenuVo.getMenuIdList();
        for (Long menuId : menuIdList) {
            if(StringUtils.isEmpty(menuId)){
                continue;
            }
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setMenuId(menuId);
            sysRoleMenu.setRoleId(assginMenuVo.getRoleId());
            sysRoleMenuService.save(sysRoleMenu);
        }
    }
    /**
     * 根据用户id获取用户可以操作的  菜单  列表
     * */
    @Override
    public List<RouterVo> findUserMenuListByUserId(Long userId) {
        List<SysMenu> sysMenuList = new ArrayList<>();
        //1 判断当前用户是否是管理员 userId = 1
          //1.1 是管理员 可查询所有菜单列表
        if(userId.longValue()==1){
            //all menu List
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus,1);
            wrapper.orderByAsc(SysMenu::getSortValue);
            sysMenuList = baseMapper.selectList(wrapper);
        }else{
          //1.2 不是管理员 根据userId查询可以操作菜单列表
            //多表关联查询:用户角色关系表、角色菜单关系表、菜单表、
            sysMenuList = baseMapper.findMenuListByUserId(userId);
        }


        //2 把查询出来的数据列表构建成框架要求的路由数据结构
        //使用菜单操作工具类构建树形结构
        List<SysMenu> sysMenuTreeList = MenuHelper.buildTree(sysMenuList);
        //构建成框架要求的路由结构
        List<RouterVo> routerList = this.buildRouter(sysMenuTreeList);
        return routerList;
    }
    //构建成框架要求的路由结构
    private List<RouterVo> buildRouter(List<SysMenu> menus) {
        //创建List集合，存储最终数据
        List<RouterVo> routers = new ArrayList<>();
        //menus遍历
        for (SysMenu menu : menus) {
            RouterVo router = new RouterVo();
            router.setHidden(false);
            router.setAlwaysShow(false);
            router.setPath(getRouterPath(menu));
            router.setComponent(menu.getComponent());
            router.setMeta(new MetaVo(menu.getName(), menu.getIcon()));
            //下一层数据部分
            List<SysMenu> children = menu.getChildren();
            //
            if(menu.getType().intValue()==1){
                //加载出来下面隐藏路由
                List<SysMenu> hiddenMenuList = children.stream().filter(item -> !StringUtils.isEmpty(item.getComponent()))
                        .collect(Collectors.toList());
                for (SysMenu hiddenMenu : hiddenMenuList) {
                    RouterVo hiddenRouter = new RouterVo();
                    //隐藏路由
                    hiddenRouter.setHidden(true);
                    hiddenRouter.setAlwaysShow(false);
                    hiddenRouter.setPath(getRouterPath(hiddenMenu));
                    hiddenRouter.setComponent(hiddenMenu.getComponent());
                    hiddenRouter.setMeta(new MetaVo(hiddenMenu.getName(), hiddenMenu.getIcon()));
                    routers.add(hiddenRouter);
                }
            }else{
                if(!CollectionUtils.isEmpty(children)){
                    if(children.size()>0){
                        router.setAlwaysShow(true);
                    }
                    /**递归*/
                    router.setChildren(buildRouter(children));
                }
            }
            routers.add(router);
        }
        return routers;
    }
    /**
     * 获取路由地址
     *
     * @param menu 菜单信息
     * @return 路由地址
     */
    public String getRouterPath(SysMenu menu) {
        String routerPath = "/" + menu.getPath();
        if(menu.getParentId().intValue() != 0) {
            routerPath = menu.getPath();
        }
        return routerPath;
    }
    /**
     *根据用户id获取用户可以操作  按钮  列表
     * */
    @Override
    public List<String> findUserPermsByUserId(Long userId) {
//        判断是否是管理员 是管理员 查询全部按钮
          List<SysMenu> sysMenuList = null;
          if(userId.longValue()==1){
              LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
              wrapper.eq(SysMenu::getStatus,1);
              sysMenuList = baseMapper.selectList(wrapper);
          }else{
              //        不是管理员 根据userId查询可以操作的按钮列表
              //    多表关联查询:用户角色关系表、角色菜单关系表、菜单表
              sysMenuList = baseMapper.findMenuListByUserId(userId);
          }

//        从查询出来的数据里面，获取可以操作按钮值的list集合返回
       List<String> permsList =  sysMenuList.stream().filter(item->item.getType()==2)
                .map(item -> item.getPerms())
                .collect(Collectors.toList());
        return permsList;
    }
}
