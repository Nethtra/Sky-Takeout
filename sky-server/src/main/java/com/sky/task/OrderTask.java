package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 35处理超时状态的订单
 *
 * @author 王天一
 * @version 1.0
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrdersMapper ordersMapper;

    /**
     * 处理超时未付款的订单  取消
     */
    @Scheduled(cron = "0 * * * * ? ")//每分钟执行一次
    public void processUnpaidOrders() {
        //先查询符合超时未付款条件的订单
        //select * from orders where status=1  and order_time<当前时间-15分钟
        //15分钟未付款     当前时间-15>下单时间
        LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(15);
        List<Orders> ordersList = ordersMapper.selectByStatusAndOrderTime(Orders.PENDING_PAYMENT, localDateTime);
        //将这些订单  设置状态为取消
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders order : ordersList) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelTime(LocalDateTime.now());
                order.setCancelReason("超时未付款，已自动取消");
                ordersMapper.update(order);
                log.warn("订单号{}超时未付款，已自动取消",order.getNumber());
            }
        }
    }

    /**
     * 处理超时未完成的订单  完成
     */
    @Scheduled(cron = "0 0 1 * * ?")//每天凌晨一点触发
    public void processDeliveryOrders() {
        LocalDateTime time = LocalDateTime.now().minusMinutes(60);
        List<Orders> ordersList = ordersMapper.selectByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, time);
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders order : ordersList) {
                order.setStatus(Orders.COMPLETED);
                ordersMapper.update(order);
            }
        }
    }
}
