package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 王天一
 * @version 1.0
 */
@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrdersMapper ordersMapper;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        //要构造两部分
        //1dateList
        //构造一个集合将日期按顺序add进去
        List<LocalDate> dateList = new ArrayList<>();
        while (!begin.equals(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        dateList.add(end);//
        //转成字符串  注意别导错包  是lang3这个包
        //接口格式要求 中间,分隔
        String join = StringUtils.join(dateList, ",");

        //2turnoverList
        //考虑如何查询每天的营业额select sum(amount) from orders where status=5 and order_time between dayBeginTime and dayEndTime
        //每天的查出来然后放到集合里
        List<Double> turnoverList = new ArrayList();
        for (LocalDate localDate : dateList) {
            //因为order_time反映到entity中是LDT类型的  所以将LocalDate转换为LDT 注意一下参数
            //具体到每一天实际就是从0到23：59
            LocalDateTime dayBeginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime dayEndTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Map map = new HashMap();
            map.put("dayBeginTime", dayBeginTime);
            map.put("dayEndTime", dayEndTime);
            map.put("status", Orders.COMPLETED);
            Double dayTurnover = ordersMapper.selectDayTurnover(map);//我也不知道为什么要写动态sql
            //当天没有营业额的话如果直接拿出来就是null  要换成0
            //而且注意使用三目运算符  比if简洁
            dayTurnover = dayTurnover == null ? 0.0 : dayTurnover;
            //别忘了这只是一天的
            turnoverList.add(dayTurnover);
        }
        String join1 = StringUtils.join(turnoverList, ",");

        return TurnoverReportVO.builder()
                .dateList(join)
                .turnoverList(join1)
                .build();
    }
}
