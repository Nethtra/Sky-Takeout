package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;

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
}
