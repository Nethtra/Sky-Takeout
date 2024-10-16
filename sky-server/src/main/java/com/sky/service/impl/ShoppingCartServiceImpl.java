package com.sky.service.impl;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 王天一
 * @version 1.0
 */
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //逻辑
        //1先判断当前购物车商品是否已经存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);//构造ShoppingCart对象
        shoppingCart.setUserId(BaseContext.getCurrentId());//设置用户id

        List<ShoppingCart> shoppingCartList = shoppingCartMapper.select(shoppingCart);//这里查
        //2已经存在就商品数量加一     update
        if (shoppingCartList != null && shoppingCartList.size() > 0) {
            //这里其实情况只有查出一条数据或者没有查出数据   看数据库表就知道
            //但是为了以后方法的通用  用List接收
            ShoppingCart shoppingCart1 = shoppingCartList.get(0);//拿出第一个 （也只有第一个）
            shoppingCart1.setNumber(shoppingCart1.getNumber() + 1);//把number+1
            shoppingCartMapper.updateNumberById(shoppingCart1);//然后再调用update再传过去
        } else {
            //3没有就插入  insert
            //判断本次添加的是菜品还是套餐
            //因为ShoppingCart中还有名称 价格等字段  所以还要来补充
            Long dishId = shoppingCart.getDishId();
            if (dishId != null) {
                //说明添加的是菜品
                Dish dish = dishMapper.selectById(dishId);
                //补充其他字段
                shoppingCart.setName(dish.getName());
                shoppingCart.setAmount(dish.getPrice());//这个amount是金额
                shoppingCart.setImage(dish.getImage());
//            shoppingCart.setNumber(1);//数量为1
//            shoppingCart.setCreateTime(LocalDateTime.now());
            } else {
                //else就是添加的是套餐
                Setmeal setmeal = setmealMapper.selectById(shoppingCart.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setImage(setmeal.getImage());
//            shoppingCart.setNumber(1);
//            shoppingCart.setCreateTime(LocalDateTime.now());
            }
            //相同的逻辑抽取出来
            shoppingCart.setNumber(1);//数量为1
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }

    }

    @Override
    public List<ShoppingCart> listShoppingCart() {
        //用user_id查
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .build();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.select(shoppingCart);
        return shoppingCartList;//直接返回
    }

    @Override
    public void cleanShoppingCart() {
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .build();
        //写动态sql  之后删除一个的时候还能用
        shoppingCartMapper.delete(shoppingCart);
    }

    @Override
    public void subAnItem(ShoppingCartDTO shoppingCartDTO) {
        //1查询number的数量是不是1
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.select(shoppingCart);
        if (shoppingCartList != null && shoppingCartList.size() > 0) {
            ShoppingCart shoppingCart1 = shoppingCartList.get(0);
            Integer number = shoppingCart1.getNumber();
            //2如果是1就直接删除
            if (number == 1)
                shoppingCartMapper.delete(shoppingCart);
                //3不是1就update把number-1
            else {
                shoppingCart1.setNumber(number - 1);
                shoppingCartMapper.updateNumberById(shoppingCart1);
            }
        }
    }
}
