package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 菜品管理
 *
 * @author 王天一
 * @version 1.0
 */
@Slf4j
@RestController
@Api("菜品管理接口")
@RequestMapping("/admin/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    /**
     * 6新增菜品   包含查询分类 文件上传 新增菜品三个子接口
     *
     * @return
     */
    @ApiOperation("新增菜品（包含口味）")
    @PostMapping
    public Result createDishWithFlavor(@RequestBody DishDTO dishDTO) {//注意用DishDTO
        log.info("添加菜品:{}", dishDTO);
        dishService.createDishWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 7菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @ApiOperation("菜品分页查询")
    @GetMapping("/page")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO) {//DTO传输数据
        log.info("菜品分页查询:{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);//统一封装PageResult对象
    }
}
