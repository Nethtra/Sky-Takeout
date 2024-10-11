package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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

    /**
     * 根据setmeal_id批量删除
     *
     * @param setmealIds
     */
    void deleteBySetmealIds(List<Long> setmealIds);

    /**
     * 根据setmeal_id查询
     *
     * @param setmealId
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id=#{setmealID} ")
    List<SetmealDish> selectBySetmealId(Long setmealId);

    /**
     * 根据setmealId删除
     *
     * @param setmealId
     */
    @Delete("delete from setmeal_dish where setmeal_id=#{setmealId} ")
    void deleteBySetmealId(Long setmealId);
}
