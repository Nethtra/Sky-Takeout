package com.sky.service;

import com.sky.vo.TurnoverReportVO;

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
}
