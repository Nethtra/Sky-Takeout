package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * 店铺管理相关接口
 *
 * @author 王天一
 * @version 1.0
 */
//由于管理端和用户端使用相同的类名   但是bean是不能重名的 会报错   所以需要手动指定bean的名字
@RestController("adminShopController")
@Slf4j
@RequestMapping("/admin/shop")
@Api("店铺管理相关接口")
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate;
    //多次出现的字符串定义为常量  规范
    public static final String KEY = "SHOP_STATUS";

    /**
     * 15设置店铺营业状态
     *
     * @param status
     * @return
     */
    @ApiOperation("设置店铺营业状态")
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status) {
        log.info("设置店铺营业状态为{}", status == 1 ? "营业中" : "打烊中");
        redisTemplate.opsForValue().set(KEY, status);//使用redis存储营业状态
        return Result.success();
    }

    /**
     * 15管理端查询店铺营业状态
     *
     * @return
     */
    @ApiOperation("管理端查询店铺营业状态")
    @GetMapping("/status")
    public Result<Integer> getStatus() {
        Integer shopStatus = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("店铺营业状态为{}", shopStatus == 1 ? "营业中" : "打烊中");
        return Result.success(shopStatus);
    }
}
