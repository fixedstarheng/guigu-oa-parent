package com.atguigu.auth.utils;

import com.atguigu.model.system.SysMenu;

import java.util.ArrayList;
import java.util.List;

/**工具类————构建树形菜单
 *
 * */
public class MenuHelper {

    /*递归
        调用递归的入口
        此次递归的结束点
    * */
    public static List<SysMenu> buildTree(List<SysMenu> sysMenuList){
        //创建List集合，用于最终数据
        List<SysMenu> trees = new ArrayList<>();
        //遍历所有菜单数据
        for (SysMenu sysMenu : sysMenuList) {
            //入口
              //parentId=0 是入口
            if(sysMenu.getParentId().longValue()==0){
                               //sysMenu此次递归根节点
                trees.add(getChildren(sysMenu,sysMenuList));
            }
        }
        return trees;
    }

    //递归方法————getChildren 查找子节点
    public static SysMenu getChildren(SysMenu sysMenu,List<SysMenu> sysMenuList){
        sysMenu.setChildren(new ArrayList<SysMenu>());
        //遍历所有菜单数据，判断id 和 parentId对应关系
        for(SysMenu it : sysMenuList){
            if(sysMenu.getId().longValue()==it.getParentId().longValue()){
               if(sysMenu.getChildren()==null){
                   sysMenu.setChildren(new ArrayList<>());
               }
               sysMenu.getChildren().add(getChildren(it,sysMenuList));
            }
        }
        return sysMenu;
    }
}
