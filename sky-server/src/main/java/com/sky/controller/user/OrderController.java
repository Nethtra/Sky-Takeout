package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
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

    }
}
