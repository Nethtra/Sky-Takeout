package com.sky.service;

import com.sky.dto.ShoppingCartDTO;

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
}
