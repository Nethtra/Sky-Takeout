package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 王天一
 * @version 1.0
 */
@RestController
@Slf4j
@RequestMapping("/user/shoppingCart")
@Api("用户端购物车相关接口")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 19用户添加购物车
     *
     * @param shoppingCartDTO
     * @return
     */
    @ApiOperation("用户添加购物车")
    @PostMapping("/add")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {//DTO
        log.info("{}被添加进购物车", shoppingCartDTO);
        shoppingCartService.addShoppingCart(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 20查看购物车
     *
     * @return
     */
    @ApiOperation("查看购物车")
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list() {
        log.info("用户查看购物车");
        List<ShoppingCart> shoppingCartList = shoppingCartService.listShoppingCart();
        return Result.success(shoppingCartList);
    }

    /**
     * 21清空购物车
     *
     * @return
     */
    @ApiOperation("清空购物车")
    @DeleteMapping("/clean")
    public Result clean() {
        log.info("用户清空购物车！");
        shoppingCartService.cleanShoppingCart();
        return Result.success();
    }

    /**
     * 22删除购物车的一个商品
     *
     * @param shoppingCartDTO
     * @return
     */
    @ApiOperation("删除购物车的一个商品")
    @PostMapping("/sub")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("用户删除商品{}",shoppingCartDTO);
        shoppingCartService.subAnItem(shoppingCartDTO);
        return Result.success();
    }
}
