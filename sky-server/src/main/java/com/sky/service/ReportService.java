package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

/**
 * @author 王天一
 * @version 1.0
 */
public interface ReportService {
    /**
     * 38统计指定时间段内每天的营业额
     *
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 39指定时间段内的用户统计
     *
     * @param begin
     * @param end
     * @return
     */
    UserReportVO userStatistics(LocalDate begin, LocalDate end);

    /**
     * 40指定时间段内的订单统计
     *
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO ordersStatistics(LocalDate begin, LocalDate end);

    /**
     * 41统计指定时间段内的销量top10
     *
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO salesTop10(LocalDate begin, LocalDate end);
}
