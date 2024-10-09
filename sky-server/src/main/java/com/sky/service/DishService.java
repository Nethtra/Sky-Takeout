package com.sky.service;

import com.sky.dto.DishDTO;

/**
 * @author 王天一
 * @version 1.0
 */
public interface DishService {
    /**
     * 6新增菜品   包含查询分类 文件上传 新增菜品三个子接口
     * @param dishDTO
     */
    void createDishWithFlavor(DishDTO dishDTO);

}
