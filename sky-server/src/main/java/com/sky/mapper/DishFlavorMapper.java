package com.sky.mapper;

import com.sky.entity.DishFlavor;
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
}
