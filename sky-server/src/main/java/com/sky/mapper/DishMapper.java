package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     *
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 6新增菜品
     *
     * @param dish
     */
    @AutoFill(OperationType.INSERT)
//别忘了可以用自动填充
    void insert(Dish dish);

    /**
     * 7菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据id查询菜品
     *
     * @param id
     * @return
     */
    @Select("select * from dish where id =#{id}")
    Dish selectById(Long id);

    /**
     * 根据id删除菜品
     *
     * @param id
     */
    @Delete("delete from dish where id =#{id}")
    void deleteById(Long id);

    /**
     * 根据id批量删除菜品
     *
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    /**
     * 修改菜品信息
     *
     * @param dish
     */
    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 10根据categoryId查询菜品
     *
     * @param dish
     * @return
     */
    @Select("select * from dish where category_id=#{categoryId} and status=#{status}")
    List<Dish> selectByCategoryId(Dish dish);

    /**
     * 根据setmeal_id查询该套餐下的所有菜品
     *
     * @param setmealId
     * @return
     */
    @Select("select dish.* from dish left join setmeal_dish " +
            "on dish.id = setmeal_dish.dish_id where setmeal_dish.setmeal_id=#{setmealId}")
    List<Dish> selectBySetmealId(Long setmealId);

    /**
     * 根据状态统计菜品数量
     *
     * @param status
     * @return
     */
    @Select("select count(*) from dish where status=#{status}")
    Integer countByStatus(Integer status);
}
