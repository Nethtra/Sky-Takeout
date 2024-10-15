package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理
 *
 * @author 王天一
 * @version 1.0
 */
@Slf4j
@RestController("adminDishController")
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
    public Result createDishWithFlavor(@RequestBody DishDTO dishDTO) {//注意用DishDTO 因为多出flavor字段 而dish表里没有口味字段
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

    /**
     * 8批量删除菜品
     *
     * @param ids
     * @return
     */
    @ApiOperation("批量删除菜品")
    @DeleteMapping
    //注意接口文档 前端传递的参数ids是String类型 使用@RequestParam标识List 可以让spring自动接收为集合
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除菜品：{}", ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 9根据id查询菜品（包含口味）
     *
     * @param id
     * @return
     */
    @ApiOperation("根据id查询菜品（包含口味信息）")
    @GetMapping("/{id}")
    public Result<DishVO> selectByIdWithFlavor(@PathVariable Long id) {
        //使用DishVO  DishVO和DishDTO的区别 VO多了categoryName和updateTime
        //理论上用DTO也行 但是为了规范使用VO   且categoryName实际上用不到 因为已经将categoryId传过去了
        //之前pageQuery菜品时使用categoryName是因为要列表展示出分类名  这里数据回显的话可以单独让前端调根据id查分类的接口
        //之前pageQuery菜品时没有用到flavors  这次要查flavors  应该用连接查询或者分开查都可以
        //但是dish连flavor感觉可能出问题 可能封装不上 最好还是分开  然后在service里封装成VO
        log.info("查询菜品：{}", id);
        DishVO dishVO = dishService.selectByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 9修改菜品数据（包含口味）
     *
     * @param dishDTO
     * @return
     */
    @ApiOperation("修改菜品数据（包含口味）")
    @PutMapping
    public Result updateWithFlavor(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品数据：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 起售停售菜品
     *
     * @param status
     * @param id
     * @return
     */
    @ApiOperation("起售停售菜品")
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("更改{}菜品状态:{}", id, status);
        dishService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 10根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @ApiOperation("根据分类id查询菜品")
    @GetMapping("/list")
    public Result<List<Dish>> selectByCategoryId(Long categoryId) {
        log.info("根据分类id查询菜品  categoryId:{}",categoryId);
        //这里看答案最好是只显示出起售的
        List<Dish> dishes = dishService.selectByCategoryId(categoryId);
        return Result.success(dishes);
    }
}
