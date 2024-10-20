package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     *
     * @param orders
     */
    void update(Orders orders);

    /**
     * 23分页查询历史订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 动态条件查询订单
     *
     * @param orders
     * @return
     */
    List<Orders> select(Orders orders);

    /**
     * 根据id查询订单
     *
     * @param id
     */
    @Select("select * from orders where id=#{id}")
    Orders selectById(Long id);
}
