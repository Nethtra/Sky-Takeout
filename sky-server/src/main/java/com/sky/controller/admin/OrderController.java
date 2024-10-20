package com.sky.controller.admin;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @GetMapping
    public Result<OrderStatisticsVO> countStatistics() {
        OrderStatisticsVO orderStatisticsVO = orderService.countStatistics();
        return Result.success(orderStatisticsVO);
    }
}
