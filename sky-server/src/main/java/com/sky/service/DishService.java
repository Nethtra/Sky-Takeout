package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

/**
 * @author 王天一
 * @version 1.0
 */
public interface DishService {
    /**
     * 6新增菜品   包含查询分类 文件上传 新增菜品三个子接口
     *
     * @param dishDTO
     */
    void createDishWithFlavor(DishDTO dishDTO);

    /**
     * 7菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 8批量删除菜品
     *
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 9根据id查询菜品（包含口味）
     *
     * @param id
     * @return
     */
    DishVO selectByIdWithFlavor(Long id);

    /**
     * 9修改菜品信息（包含口味）
     *
     * @param dishDTO
     */
    void updateWithFlavor(DishDTO dishDTO);
}
