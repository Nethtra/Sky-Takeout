package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderReportVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author 王天一
 * @version 1.0
 */
@RequestMapping("/user/order")
@RestController("userOrderController")
@Slf4j
@Api("用户端订单接口")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 23用户下单功能
     *
     * @param ordersSubmitDTO
     * @return
     */
    @ApiOperation("用户下单")
    @PostMapping("/submit")//点击 去支付 时会请求到这里
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        //根据请求参数和返回数据   决定接收参数类型是DTO   返回类型为VO
        log.info("用户下单{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付   用户点击支付按钮（第一次）
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);//封装好返回给前端，然后用来让前端真正调起支付（wx.requestpayment）
    }

    /**
     * 23分页查询历史订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @ApiOperation("分页查询所有历史订单")
    @GetMapping("/historyOrders")
    public Result<PageResult> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("分页查询历史订单{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.pageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 24根据id查询订单详情
     *
     * @param id
     * @return
     */
    @ApiOperation("根据id查询订单详情")
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> orderDetail(@PathVariable Long id) {
        log.info("查询id为{}的订单详情", id);
        OrderVO orderVO = orderService.orderDetail(id);
        return Result.success(orderVO);
    }

    /**
     * 25取消订单
     *
     * @param id
     * @return
     */
    @ApiOperation("根据id取消订单")
    @PutMapping("/cancel/{id}")
    public Result cancelOrder(@PathVariable Long id) throws Exception {
        log.info("取消订单{}", id);
        orderService.cancelOrder(id);
        return Result.success();
    }

    /**
     * 26再来一单
     *
     * @param id
     * @return
     */
    @ApiOperation("再来一单")
    @PostMapping("/repetition/{id}")
    public Result repetition(@PathVariable Long id) {
        log.info("再来一单{}", id);
        orderService.repetition(id);
        return Result.success();
    }

    /**
     * 37 用户催单
     *
     * @param id
     * @return
     */
    @ApiOperation("用户催单")
    @GetMapping("/reminder/{id}")
    public Result reminder(@PathVariable Long id){
        log.info("用户催{}单",id);
        orderService.reminder(id);
        return Result.success();
    }
}
