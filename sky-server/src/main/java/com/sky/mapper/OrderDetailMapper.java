package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author 王天一
 * @version 1.0
 */
@Mapper
public interface OrderDetailMapper {
    /**
     * 批量插入一个订单的订单明细数据
     *
     * @param orderDetailList
     */
    void insertBatch(List<OrderDetail> orderDetailList);
}
