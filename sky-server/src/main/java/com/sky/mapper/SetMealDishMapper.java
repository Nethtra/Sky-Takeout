package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author 王天一
 * @version 1.0
 */
@Mapper
public interface SetMealDishMapper {
    /**
     * 8根据dishIds查询setmealIds
     *
     * @param DishIds
     * @return
     */
    List<Long> selectSetMealIdsByDishIds(List<Long> DishIds);

    /**
     * 10批量新增套餐和菜品的关系
     *
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);
}
