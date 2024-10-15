package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 王天一
 * @version 1.0
 */
@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetMealDishMapper setMealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    @Override
    @Transactional//一定要开事务 第一次测试insertBatch的sql出错 导致只插了dish表
    public void create(SetmealDTO setmealDTO) {
        //应该是要分别插setmeal表和setmeal_dish表
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);//useGeneratedKeys="true" keyProperty="id"

        //setmeal_dish表
        //新增时 setmealDish中没有setmealId
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmeal.getId());//设置setmeal_id
        });
        //批量新增
        setMealDishMapper.insertBatch(setmealDishes);
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        //setmeal表中也没有 category_name字段 而接口文档中需要返回  需要使用VO且使用外连接查询
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //判断套餐是否起售
        ids.forEach(id -> {
            Setmeal setmeal = setmealMapper.select(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE)
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        });
        setmealMapper.deleteByIds(ids);//删除setmeal表中数据
        setMealDishMapper.deleteBySetmealIds(ids);//删除setmeal_dish表中的数据
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        //起售时先判断套餐里是否有还在停售状态的dish 有就不能起售
        //要根据setmeal_id查询到dish的status  可以关联起dish和setmeal_dish来查 因为分开查就要先用setmeal_id查setmeal_dish 查出dish_id 再用dish_id查dish的status
        //且可以直接查出个Dish集合 然后遍历集合来判断dish的status
        //理解联合查询  想用套餐id查寻菜品  但是需要先后查两张表  因为菜品中没有直接的套餐id字段
        //所以可以将两张表用on菜品id关联起来成一张表  然后使用where setmeal_id查
        if (status == StatusConstant.ENABLE) {//如果要起售
            List<Dish> dishes = dishMapper.selectBySetmealId(id);//将查出的dish封装成集合
            if (dishes != null && dishes.size() > 0) {
                dishes.forEach(dish -> {
                    if (dish.getStatus() == StatusConstant.DISABLE)//检查菜品状态
                        //如果有未起售的就抛异常
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                });
            }
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);//使用公共的update方法
    }

    @Override
    public SetmealVO select(Long id) {
        //先查setmeal表
        Setmeal setmeal = setmealMapper.select(id);
        //查setmeal_dish表
        List<SetmealDish> setmealDishes = setMealDishMapper.selectBySetmealId(id);
        //封装成VO
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Override
    public void updateWithDishes(SetmealDTO setmealDTO) {
        //要改两张表
        //setmeal
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);
        //setmeal_dish
        //先删除再重新添加
        setMealDishMapper.deleteBySetmealId(setmealDTO.getId());//删除
        //重新添加时 private List<SetmealDish> setmealDishes 中的setmealdish少了setmealId 需要手动补
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealDTO.getId());
        });
        setMealDishMapper.insertBatch(setmealDishes);//再批量插入
    }

    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> setmeals = setmealMapper.selectByCategoryId(setmeal);
        return setmeals;
    }

    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        //注意 外连接查询
        List<DishItemVO> dishItemVOS = setmealMapper.selectBySetmealId(id);
        return dishItemVOS;
    }
}
