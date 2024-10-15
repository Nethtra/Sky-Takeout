package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 套餐管理
 *
 * @author 王天一
 * @version 1.0
 */
@RestController("adminSetmealController")
@Api("套餐管理相关接口")
@Slf4j
@RequestMapping("/admin/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    /**
     * 10新增套餐 包含查询套餐分类 上传图片 根据分类id查询菜品 新增套餐 四个子接口
     *
     * @param setmealDTO
     * @return
     */
    @ApiOperation("新增套餐")
    @PostMapping
    @CacheEvict(cacheNames = "setmealCache",key = "#setmealDTO.categoryId")//缓存
    public Result create(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐:{}", setmealDTO);
        setmealService.create(setmealDTO);
        return Result.success();
    }

    /**
     * 11套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @ApiOperation("套餐分页查询")
    @GetMapping("/page")
    public Result<PageResult> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("套餐分页查询：{}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 12批量删除套餐
     *
     * @param ids
     * @return
     */
    @ApiOperation("批量删除套餐")
    @DeleteMapping
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)//缓存
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除套餐：{}", ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 13起售停售套餐
     *
     * @param status
     * @param id
     * @return
     */
    @ApiOperation("起售停售套餐")
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)//缓存
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("套餐{} 设置状态{}", id, status);
        setmealService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 14根据id查询套餐（包括dish）  数据回显
     *
     * @param id
     * @return
     */
    @ApiOperation("根据id查询套餐(包括dish)")
    @GetMapping("/{id}")
    public Result<SetmealVO> select(@PathVariable Long id) {//使用VO
        log.info("查询套餐id{}", id);
        SetmealVO setmealVO = setmealService.select(id);
        return Result.success(setmealVO);
    }

    /**
     * 14修改套餐信息（包含setmeal_dish表）
     *
     * @param setmealDTO
     * @return
     */
    @ApiOperation("修改套餐信息（包含setmeal_dish表）")
    @PutMapping
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)//缓存
    public Result updateWithDishes(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改套餐信息{}", setmealDTO);
        setmealService.updateWithDishes(setmealDTO);
        return Result.success();
    }
}
