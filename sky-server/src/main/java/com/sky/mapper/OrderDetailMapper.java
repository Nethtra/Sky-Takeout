package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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

    /**
     * 根据order_id查询订单明细
     *
     * @param orderId
     * @return
     */
    @Select("select * from order_detail where order_id=#{orderID}")
    List<OrderDetail> selectByOrderId(Long orderId);
}
