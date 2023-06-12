package com.atguigu.wechat.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.auth.service.SysUserService;
import com.atguigu.common.jwt.JwtHelper;
import com.atguigu.common.result.Result;
import com.atguigu.vo.wechat.BindPhoneVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import com.atguigu.model.system.SysUser;

@Controller //为了页面能重定向 没有使用RestController
@RequestMapping("/admin/wechat")
@Slf4j
@CrossOrigin
//@CrossOrigin
public class WechatController {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private WxMpService wxMpService;
    @Value("${wechat.userInfoUrl}")
    private String userInfoUrl;

    /*授权成功 返回路径*/
    @GetMapping("/authorize")
    public String authorize(@RequestParam("returnUrl") String returnUrl, HttpServletRequest request){
        System.out.println("进入后端回调 returnUrl"+returnUrl);
          //buildAuthorizationUrl Params:
                //授权路径 redirectUri – 用户授权完成后的重定向链接（获取微信信息连接），无需urlencode, 方法内会进行
                //授权类型 encode scope – scope,静默:snsapi_base, 带信息授权:snsapi_userinfo
                //授权状态 state – state
        System.out.println("授权成功  返回路径 authorize///______");
        String redirectUrl = null;
        try {
            redirectUrl = wxMpService.getOAuth2Service().buildAuthorizationUrl(userInfoUrl,
                    WxConsts.OAuth2Scope.SNSAPI_USERINFO,
                    URLEncoder.encode(returnUrl.replace("guiguoa", "#"),"utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        System.out.println("授权成功  返回路径 redirectUrl///______"+redirectUrl);
        return "redirect:"+redirectUrl;

    }

    /*获取授权信息*/
    @GetMapping("/userInfo")
    public String userInfo(@RequestParam("code") String code,
                           @RequestParam("state") String returnUrl) throws Exception {
        System.out.println("获取授权信息///______");
        //获取accessToken
        WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
        //使用accessToken获取openId
        String openId = accessToken.getOpenId();
        System.out.println("openId///————: "+openId);
        //get weixin user info
        WxOAuth2UserInfo wxMpUser = wxMpService.getOAuth2Service().getUserInfo(accessToken, null);
        System.out.println("微信用户信息 wxMpUser///————: "+ JSON.toJSONString(wxMpUser));
        //select user by openId is exist? create token and  redirectUrl : 绑定
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getOpenId,openId);
        SysUser sysUser = sysUserService.getOne(wrapper);
        String token = "";
        if(sysUser!=null){
            token = JwtHelper.createToken(sysUser.getId(),sysUser.getUsername());
        }if(returnUrl.indexOf("?") == -1) {
            System.out.println("授权成功  返回路径 redirectUrl///______"+returnUrl + "?token=" + token + "&openId=" + openId);
            return "redirect:" + returnUrl + "?token=" + token + "&openId=" + openId;
        } else {
            return "redirect:" + returnUrl + "&token=" + token + "&openId=" + openId;
        }
    }

    /*微信账号绑定手机号  手机号在用户表是否存在*/
    @ApiOperation(value = "微信账号绑定手机")
    @PostMapping("bindPhone")
    @ResponseBody
    public Result bindPhone(@RequestBody BindPhoneVo bindPhoneVo) {
        System.out.println("微信账号绑定手机///______");
        //根据手机号查数据库 用户存在不
        SysUser sysUser = sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, bindPhoneVo.getPhone()));
        //存在，可以绑定
        if(null != sysUser) {
            sysUser.setOpenId(bindPhoneVo.getOpenId());
            sysUserService.updateById(sysUser);

            String token = JwtHelper.createToken(sysUser.getId(), sysUser.getUsername());
            return Result.ok(token);
        } else {//不存在无法绑定
            return Result.fail("手机号码不存在，绑定失败");
        }
    }
}
