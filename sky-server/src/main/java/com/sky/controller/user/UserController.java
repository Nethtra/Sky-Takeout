package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 王天一
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/user/user")//第一个user是用户端  第二个user代表用户模块
@Api("微信小程序用户相关接口")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 16用户微信登陆
     *
     * @param userLoginDTO
     * @return
     */
    @ApiOperation("用户微信登陆")
    @PostMapping("/login")
    public Result<UserLoginVO> longin(@RequestBody UserLoginDTO userLoginDTO) {//虽然前端只有code  但也用DTO
        log.info("用户请求微信登陆，授权码code{}", userLoginDTO.getCode());
        //先登陆
        User user = userService.wxLogin(userLoginDTO);//Service中返回类型定义为User 因为只会携带id和openid 用其他的不合适 还有jwt生成要在controller中做
        //登陆成功后下发构造jwt令牌  所以这么看login和构造jwt令牌确实不能全放在service里
        //注意直接注入jwt的配置属性类
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        //封装成VO返回
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();
        return Result.success(userLoginVO);
    }
}
