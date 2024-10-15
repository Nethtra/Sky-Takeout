package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author 王天一
 * @version 1.0
 */
public interface SetmealService {
    /**
     * 10新增套餐
     *
     * @param setmealDTO
     */
    void create(SetmealDTO setmealDTO);

    /**
     * 11套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 12批量删除套餐
     *
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 13起售停售套餐
     *
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 14根据id查询套餐
     *
     * @param id
     * @return
     */
    SetmealVO select(Long id);

    /**
     * 14修改套餐信息（包含setmeal_dish表）
     *
     * @param setmealDTO
     */
    void updateWithDishes(SetmealDTO setmealDTO);

    /**
     * 18商品浏览：根据分类id查询套餐
     *
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 18商品浏览：根据setmealId查询菜品的部分信息
     *
     * @param id
     * @return
     */

    List<DishItemVO> getDishItemById(Long id);
}
