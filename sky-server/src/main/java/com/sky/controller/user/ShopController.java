package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端店铺相关接口
 *
 * @author 王天一
 * @version 1.0
 */
@RestController("userShopController")
@Slf4j
@RequestMapping("/user/shop")
@Api("用户端店铺管理相关接口")
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate;
    public static final String KEY = "SHOP_STATUS";

    /**
     * 15用户端查询店铺营业状态
     *
     * @return
     */
    @ApiOperation("用户端查询店铺营业状态")
    @GetMapping("/status")
    public Result<Integer> getStatus() {
        Integer shopStatus = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("店铺营业状态为{}", shopStatus == 1 ? "营业中" : "已打烊");
        return Result.success(shopStatus);
    }
}
