package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     *
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 10新增套餐
     *
     * @param setmeal
     */
    @AutoFill(OperationType.INSERT)
    void insert(Setmeal setmeal);

    /**
     * 11分页查询套餐（外连接查询 category_name）
     *
     * @param setmealPageQueryDTO
     * @return
     */
    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 查询套餐
     *
     * @param id
     * @return
     */
    @Select("select * from setmeal where id=#{id}")
    Setmeal selectById(Long id);

    /**
     * 批量删除套餐
     *
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    /**
     * 修改套餐信息
     *
     * @param setmeal
     */
    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    /**
     * 根据分类id查询套餐
     *
     * @param setmeal
     * @return
     */
    @Select("select * from setmeal where category_id=#{categoryId} and status=#{status}")
    List<Setmeal> selectByCategoryId(Setmeal setmeal);

    /**
     * 18商品浏览：根据setmealId查询dish的部分信息 外连接查询
     *
     * @param setmealId
     * @return
     */
    @Select("select setmeal_dish.copies,dish.name,dish.image ,dish.description " +
            "from setmeal_dish left join dish on setmeal_dish.dish_id = dish.id " +
            "where setmeal_id=#{setmealId}")
    List<DishItemVO> selectBySetmealId(Long setmealId);
}
