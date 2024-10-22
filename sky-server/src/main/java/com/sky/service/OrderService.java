package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

/**
 * @author 王天一
 * @version 1.0
 */
public interface OrderService {
    /**
     * 23用户下订单
     *
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 23分页查询历史订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 24根据id查询订单详情
     *
     * @param id
     * @return
     */
    OrderVO orderDetail(Long id);

    /**
     * 25取消订单
     *
     * @param id
     */
    void cancelOrder(Long id) throws Exception;

    /**
     * 26再来一单
     *
     * @param id
     */
    void repetition(Long id);

    /**
     * 27搜索订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 28统计各状态订单的数量
     *
     * @return
     */
    OrderStatisticsVO countStatistics();

    /**
     * 30商家接单
     *
     * @param ordersConfirmDTO
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 31商家拒单
     *
     * @param ordersRejectionDTO
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception;

    /**
     * 32商家取消订单
     *
     * @param ordersCancelDTO
     */
    void merchantCancelOrder(OrdersCancelDTO ordersCancelDTO);

    /**
     * 派送订单
     *
     * @param id
     */
    void deliveryOrder(Long id);

    /**
     * 34完成订单
     *
     * @param id
     */
    void completeOrder(Long id);

    /**
     * 37用户催单
     *
     * @param id
     */
    void reminder(Long id);
}
