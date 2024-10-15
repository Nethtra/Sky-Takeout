package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 王天一
 * @version 1.0
 */
@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetMealDishMapper setMealDishMapper;

    @Transactional//因为要同时插两张表  开启事务防止出问题
    @Override
    public void createDishWithFlavor(DishDTO dishDTO) {
        //提交的数据可能包含口味  且Dish表里没有口味字段  所以这是分为两次插入
        //Dish表插入1条数据  Flavor表插入n条数据

        //向Dish表
        //DishDTO里多出flavors字段 需要先分开
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
        //插入完成后dishId已经返回   所以要先插Dish
        //向Flavor表
        List<DishFlavor> flavors = dishDTO.getFlavors();//先拿出DTO里的口味
        if (flavors != null && flavors.size() > 0) {

            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dish.getId());//设置dishId
            });


            //用xml 动态sql实现批量插入
            dishFlavorMapper.insertBatch(flavors);//还有一个问题  因为是一起插的 所以并没有dishId
            //如何获取dishId:useGeneratedKeys 参数只针对 insert 语句生效，默认为 false。
            //当设置为 true 时，表示如果插入的表以自增列为主键，则允许 JDBC 支持自动生成主键，并可将自动生成的主键返回。
            //所以Dish的insert也要使用xml
        }
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        //pagehelper
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        //注意这里的泛型是DishVO 因为接口文档里返回的数据多出一个字段categoryName不是dish表里的 所以要用一个新的VO类来封装
        //mapper中使用外连接查询  也要把categoryName查出来
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional//开启事务
    public void deleteBatch(List<Long> ids) {
        //注意删除的逻辑
        //1判断dish是否起售
        for (Long id : ids) {
            Dish dish = dishMapper.selectById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                //如果起售抛异常不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //2判断是否被套餐关联
        //思路是根据dishId去查setmealid 如果有的关联的话就不给删
        //这里和上面不一样直接查ids了 我觉得是因为dish的selectById是能复用的方法 而这个setmeal_dish表一般不会查 所以写个直接一点的方法
        List<Long> setMealIds = setMealDishMapper.selectSetMealIdsByDishIds(ids);
        if (setMealIds != null && setMealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //3删除dish
        //这样会有性能问题 会执行多次sql导致性能下降
/*        for (Long id :ids) {
            dishMapper.deleteById(id);
            //4删除dish关联的flavor
            //不用管有没有关联 直接删
            dishFlavorMapper.deleteByDishId(id);
        }*/
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }

    @Override
    public DishVO selectByIdWithFlavor(Long id) {
        DishVO dishVO = new DishVO();
        //分开两张表查
        Dish dish = dishMapper.selectById(id);//查dish
        List<DishFlavor> dishFlavors = dishFlavorMapper.selectByDishId(id);//查flavor 注意用集合接收
        //然后封装到VO
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        //分别修改dish和flavor表
        //修改dish表
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        //修改flavor表
        //对于flavor表，不好直接修改 因为一个菜品有很多口味
        //所以可以先删除 再重新添加  达到修改的效果
        dishFlavorMapper.deleteByDishId(dishDTO.getId());//删除
        //因为前端也没有传Flavor的dishid 所以还要自己赋  只不过这次dishDTO已经包含了dishId
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
        }
        dishFlavorMapper.insertBatch(flavors);//重新添加
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);
    }

    @Override
    public List<Dish> selectByCategoryId(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        List<Dish> dishes = dishMapper.selectByCategoryId(dish);
        return dishes;
    }

    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        //注意是根据分类id查询菜品
        //查出菜品的集合  每一个菜品又有多个口味
        //同样是分两次查询  只不过这次是DishVO的集合
        List<Dish> dishes = dishMapper.selectByCategoryId(dish);//查出的是Dish集合
        //要转成VO类型的List返回
        List<DishVO> dishVOList = new ArrayList<>();
        for (Dish aDish : dishes) {
            //查询口味
            List<DishFlavor> dishFlavors = dishFlavorMapper.selectByDishId(aDish.getId());
            //构造每一个DishVO
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(aDish, dishVO);
            dishVO.setFlavors(dishFlavors);
            dishVOList.add(dishVO);
        }
        //返回List<DishVO>
        return dishVOList;

    }
}
