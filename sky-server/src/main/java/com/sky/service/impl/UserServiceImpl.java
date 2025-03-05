package com.sky.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 王天一
 * @version 1.0
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;
    public static final String WX_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
/*        //与管理端登陆的思路不同
        //1获取用户的openid   参照微信官方的开发文档
        //https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html
        Map<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());//设置请求参数
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", userLoginDTO.getCode());
        map.put("grant_type", "authorization_code");
        String reponse = HttpClientUtil.doGet(WX_URL, map);//向微信的接口发起请求
        //返回的数据是json格式的字符串
        JSONObject jsonObject = JSON.parseObject(reponse);//fastjson包
        String openId = jsonObject.getString("openid");//获取openid*/

        //将上面的方法抽取成公共方法  因为很固定
        String openId = getOpenId(userLoginDTO);
        //2如果没有openid就抛异常
        if (openId == null)
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        //3如果有openid 判断是否为新用户
        User user = userMapper.selectByOpenId(openId);
        //4是新用户就插入数据（自动注册）
        if (user == null) {
            user = User.builder()
                    .openid(openId)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);//这里注意插入数据时要拿回主键id  因为在controller中要用到id设置claims 所以使用动态sql
        }
        //5返回User
        return user;
    }

    /**
     * 16抽取的获取微信用户的openid方法
     *
     * @param userLoginDTO
     * @return
     */
    //只有当前类用到 private即可
    private String getOpenId(UserLoginDTO userLoginDTO) {
        //与管理端登陆的思路不同
        //1获取用户的openid   参照微信官方的开发文档
        //https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html
        Map<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());//设置请求参数
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", userLoginDTO.getCode());
        map.put("grant_type", "authorization_code");
        String reponse = HttpClientUtil.doGet(WX_URL, map);//向微信的接口发起请求
        //返回的数据是json格式的字符串
        JSONObject jsonObject = JSON.parseObject(reponse);//fastjson包
        String openId = jsonObject.getString("openid");//获取openid
        return openId;
    }
}
