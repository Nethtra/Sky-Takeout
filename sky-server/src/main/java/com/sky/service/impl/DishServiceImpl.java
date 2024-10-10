package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
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
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

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
}
