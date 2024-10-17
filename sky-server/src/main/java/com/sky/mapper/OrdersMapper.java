package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 王天一
 * @version 1.0
 */
@Mapper
public interface OrdersMapper {
    /**
     * 向订单表中插入一条数据
     *
     * @param orders
     */
    void insert(Orders orders);
}
