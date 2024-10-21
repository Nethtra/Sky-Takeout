package com.sky.controller.admin;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
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
@RestController("adminOrderController")
@Slf4j
@RequestMapping("/admin/order")
@Api("管理端订单相关接口")
public class OrderController {
    @Autowired
    private OrderService orderService;


    /**
     * 27订单搜索  需要分页
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @ApiOperation("搜索订单")
    @GetMapping("/conditionSearch")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        //条件+分页查询
        log.info("搜索订单{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 28 各状态订单数数量统计
     *
     * @return
     */
    @ApiOperation("统计各状态订单的数量")
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> countStatistics() {
        OrderStatisticsVO orderStatisticsVO = orderService.countStatistics();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 29查询订单详情
     *
     * @param id
     * @return
     */
    @ApiOperation("查询订单详情")
    @GetMapping("/details/{id}")
    public Result<OrderVO> orderDetails(@PathVariable Long id) {
        //这里遇到一个bug  直接调以前的方法 出现索引越界异常  查找原因发现应该是orderDetail(id)中 Orders orders1 = ordersList.get(0);
        //出了问题 所以是sql没有查出来  查看xml发现以前写的sql select使用了动态条件if test=userId!=null
        //而在管理端获取的id是员工的id  在orderDetail(id)方法中set时set成了员工的id   所以没有查到 就是令牌的id串了
        //所以要不就是改sql注释掉userId的if  或者改orderDetail(id)  不使用userId作为条件查询
        log.info("查询{}订单详情", id);
        OrderVO orderVO = orderService.orderDetail(id);//直接调以前的
        return Result.success(orderVO);
    }

    /**
     * 30 商家接单
     *
     * @param id
     * @return
     */
    @ApiOperation("商家接单")
    @PutMapping("/confirm")
    public Result confirm(@RequestBody Long id) {
        log.info("商家接单{}", id);
        orderService.confirm(id);
        return Result.success();
    }
}
