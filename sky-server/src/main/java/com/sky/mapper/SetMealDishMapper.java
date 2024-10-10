package com.sky.mapper;

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
}
