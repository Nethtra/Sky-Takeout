package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    /**
     * 查询特定状态的订单数量
     *
     * @param status
     * @return
     */
    @Select("select count(*) from orders where status=#{status}")
    Integer countOrdersByStatus(Integer status);

    /**
     * 根据状态和下单时间查询订单
     *
     * @param status
     * @param time
     * @return
     */
    @Select("select * from orders where status=#{status} and order_time<#{time} ")
    List<Orders> selectByStatusAndOrderTime(Integer status, LocalDateTime time);

    /**
     * 统计的营业额
     *
     * @param map
     * @return
     */
    Double selectDayTurnoverByMap(Map map);

    /**
     * 统计订单数
     *
     * @param map
     * @return
     */
    Integer countOrdersByMap(Map map);

    /**
     * 统计指定时间段销量top10的商品
     *
     * @param BeginTime
     * @param EndTime
     * @return
     */
    List<GoodsSalesDTO> selectSalesTop10(LocalDateTime BeginTime, LocalDateTime EndTime);
}
