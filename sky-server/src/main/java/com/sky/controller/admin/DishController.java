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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

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
    @Autowired
    private RedisTemplate redisTemplate;

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
        redisTemplate.delete("dish_" + dishDTO.getCategoryId());//精准清除缓存
        //其实新增不用清  因为新增默认停售 不会被展示
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
        //批量删除涉及多个类 因为缓存是按菜品分类分的  所以不好精准清  直接删除所有
        /*Set keys = redisTemplate.keys("dish_*");//匹配所有key
        redisTemplate.delete(keys);//删除所有*/
        cleanCache("dish_*");
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
        cleanCache("dish_*");//修改因为可能修改分类  一修改分类就会涉及到多个缓存  所以也直接清
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
        cleanCache("dish_*");//启售停售如果要精准清的话 需要再查一遍数据库category_id 得不偿失
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
        log.info("根据分类id查询菜品  categoryId:{}", categoryId);
        //这里看答案最好是只显示出起售的
        List<Dish> dishes = dishService.selectByCategoryId(categoryId);
        return Result.success(dishes);
    }

    /**
     * 清理redis的所有缓存 抽取的公共方法 传入的是pattern 匹配模式
     *
     * @param pattern
     */
    private void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);//匹配所有key
        redisTemplate.delete(keys);//删除所有
    }
}
