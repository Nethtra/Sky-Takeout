package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

/**
 * @author 王天一
 * @version 1.0
 */
public interface ShoppingCartService {
    /**
     * 19添加购物车
     *
     * @param shoppingCartDTO
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 20用户查看购物车
     *
     * @return
     */
    List<ShoppingCart> listShoppingCart();
}
