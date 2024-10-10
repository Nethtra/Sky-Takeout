package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author 王天一
 * @version 1.0
 */
@Mapper
public interface DishFlavorMapper {
    /**
     * 6批量新增菜品口味
     *
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据DishId删除口味
     *
     * @param dishId
     */
    @Delete("delete from dish_flavor where dish_id=#{dishId}")
    void deleteByDishId(Long dishId);

    /**
     * 根据DishIds批量删除口味
     *
     * @param dishIds
     */
    void deleteByDishIds(List<Long> dishIds);
}
