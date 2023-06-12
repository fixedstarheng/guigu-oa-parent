package com.atguigu.wechat.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.model.wechat.Menu;
import com.atguigu.vo.wechat.MenuVo;
import com.atguigu.wechat.mapper.MenuMapper;
import com.atguigu.wechat.service.MenuService;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.SneakyThrows;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * wechat_menu表
 *
 * @author lhy
 * @since 2023-06-07
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {
    @Autowired
    private WxMpService wxMpService;

    /*获取全部菜单  （树形结构）*/
        /*
             1.查询所有菜单list集合 只有数据 没有结构
             2.查询所有一级菜单 parent_id = 0 ,返回一级菜单list集合
             3.一级菜单list集合遍历，得到每个一级菜单
             4.获取每个一级菜单里面所有二级菜单 id 和 parent_id比较
             5.把一级菜单里面所有二级菜单获取到，封装一级菜单children集合里面
             涉及Menu和MenuVo类型转换
         */
    @Override
    public List<MenuVo> findMenuInfo() {
        List<MenuVo> list = new ArrayList<>();
        //所有menu集合
        List<Menu> menuList = baseMapper.selectList(null);
        //一级菜单(parentid = 0) 所有集合
        List<Menu> oneMenuList = menuList.stream()
                .filter(menu -> menu.getParentId().longValue() == 0)
                .collect(Collectors.toList());
        //一级菜单所有转换 menu-》menuVo
        for (Menu oneMenu : oneMenuList) {
            MenuVo oneMenuVo = new MenuVo();
            //类型转换
            BeanUtils.copyProperties(oneMenu, oneMenuVo);
            //获取一级菜单的（二级菜单）子菜单 id 和 parent_id比较
            //其他菜单parentId 和 一级菜单id
            List<Menu> twoMenuList = menuList.stream().filter(menu -> menu.getParentId().longValue() == oneMenu.getId()).sorted(Comparator.comparing(Menu::getSort)).collect(Collectors.toList());
            //5.把一级菜单里面所有二级菜单获取到，封装一级菜单children集合里面
            List<MenuVo> children = new ArrayList<>();
            for (Menu twoMenu : twoMenuList) {
                MenuVo tewMenuVo = new MenuVo();
                //类型转换
                BeanUtils.copyProperties(twoMenu, tewMenuVo);
                children.add(tewMenuVo);
            }
            oneMenuVo.setChildren(children);
            list.add(oneMenuVo);
        }
        return list;
    }

    /*同步菜单 接口*/
        /*
            1.菜单数据查询出来，封装微信要求菜单格式
            2.调用工具里面的方法实现菜单推送

        */
    @Override
    public void syncMenu() {
        System.out.println("同步菜单 接口 syncMenu///——————");
        List<MenuVo> menuVoList = this.findMenuInfo();
        //菜单
        JSONArray buttonList = new JSONArray();
        for(MenuVo oneMenuVo : menuVoList) {
            JSONObject one = new JSONObject();
            one.put("name", oneMenuVo.getName());
            if(CollectionUtils.isEmpty(oneMenuVo.getChildren())) {
                one.put("type", oneMenuVo.getType());
                one.put("url", "http://192.168.0.161:9090/#"+oneMenuVo.getUrl());
            } else {
                JSONArray subButton = new JSONArray();
                for(MenuVo twoMenuVo : oneMenuVo.getChildren()) {
                    JSONObject view = new JSONObject();
                    view.put("type", twoMenuVo.getType());
                    if(twoMenuVo.getType().equals("view")) {
                        view.put("name", twoMenuVo.getName());
                        //H5页面地址
                        view.put("url", "http://192.168.0.161:9090#"+twoMenuVo.getUrl());
                    } else {
                        view.put("name", twoMenuVo.getName());
                        view.put("key", twoMenuVo.getMeunKey());
                    }
                    subButton.add(view);
                }
                one.put("sub_button", subButton);
            }
            buttonList.add(one);
        }
        //菜单
        JSONObject button = new JSONObject();
        button.put("button", buttonList);
        try {
            wxMpService.getMenuService().menuCreate(button.toJSONString());
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }

    /*删除同步菜单*/
    @SneakyThrows
    @Override
    public void removeMenu() {
        wxMpService.getMenuService().menuDelete();
    }
}
