package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 王天一
 * @version 1.0
 */
@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;//service也可以注入到service

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
            Double dayTurnover = ordersMapper.selectDayTurnoverByMap(map);//我也不知道为什么要写动态sql
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

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        //dateList
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //查用户数量实际就是 根据user 表里的create_time来区分
        //select count(*) from user where create_time<
        List<Long> totalUserList = new ArrayList<>();
        List<Long> newUserList = new ArrayList<>();
        int i = 0;
        for (LocalDate localDate : dateList) {
            LocalDateTime dayEndTime = LocalDateTime.of(localDate, LocalTime.MAX);
            LocalDateTime dayBeginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            Map map = new HashMap();
            map.put("dayEndTime", dayEndTime);
            //先查总用户数 因为只有一个参数  这样可以复用map
            Long totalUser = userMapper.countUserByMap(map);
            //查新增用户数
            Long newUser;
            if (i == 0) {
                map.put("dayBeginTime", dayBeginTime);
                newUser = userMapper.countUserByMap(map);
            } else {
                //优化了一下逻辑
                //后面天数的新增用户  可以用当天的总用户减去前一天的总用户 来减少查询次数
                newUser = totalUser - totalUserList.get(i - 1);
            }
            totalUserList.add(totalUser);
            newUserList.add(newUser);
            i++;
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        //OrderReportVO里有很多字段
        //梳理
        //三个由List转成的String dateList（日期列表） orderCountList（当日订单总数）  validOrderCountList（当日有效订单数）
        //状态为已完成的订单就是有效订单
        //dateList
        List<LocalDate> dateList = new ArrayList<>();
        while (!begin.equals(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        dateList.add(end);
        //orderCountList  select count(*) from orders where order_time>   and order_time<
        //validOrderCountList  select count(*) from orders where order_time>   and order_time<   and status=5
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            LocalDateTime dayBeginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime dayEndTime = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map = new HashMap<>();
            map.put("dayBeginTime", dayBeginTime);
            map.put("dayEndTime", dayEndTime);
            //所以用map的好处就是  参数个数不同的时候可以复用
            Integer orderCount = ordersMapper.countOrdersByMap(map);
            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = ordersMapper.countOrdersByMap(map);
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }

        //剩下的三个数值类型totalOrderCount  validOrderCount    orderCompletionRate
        //将集合中的元素累加然后返回
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();//时间段内的订单总数
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();//时间段内的有效订单数
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0)//考虑除0的情况
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;//订单完成率

        //封装返回
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO salesTop10(LocalDate begin, LocalDate end) {
        //返回数据需要 菜品或套餐名称列表和对应销量列表  虽然名称和销量都在order_detail表中
        //但注意隐含的条件 订单需要完成   所以要连接orders表将status=5作为查询条件之一
        //其实还需要orders中的时间
        //注意sql的写法  分组  然后降序  然后前十条limit
        //隐式内连接
        //注意这里的sum(od.number)一定要起别名 number 要不会封装不上
        //select od.name,od.sum(number) **number** from orders o,order_detail od where o.id=od.order_id
        //and o.status=5 and o.order_time> and o.order_time<
        //group by od.name  order by  od.sum(number) desc limit 0,10
        //显示内连接
        //select od.name,od.sum(number) **number** from orders o join order_detail od on o.id=od.order_id
        //where o.status=5 and o.order_time> and o.order_time<
        //group by od.name  order by  od.sum(number) desc limit 0,10
        LocalDateTime BeginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime EndTime = LocalDateTime.of(end, LocalTime.MAX);
        //注意mapper中的返回类型，一个包含商品名称和销量的类的集合  GoodsSalesDTO
        List<GoodsSalesDTO> goodsSalesDTOS = ordersMapper.selectSalesTop10(BeginTime, EndTime);
        //将name和number从集合中的GoodsSalesDTO分别拿出来然后收集成新的集合
        //两种写法
        List<String> names = goodsSalesDTOS.stream().map(goodsSalesDTO -> {
            return goodsSalesDTO.getName();
        }).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");//转成字符串
        List<Integer> numbers = goodsSalesDTOS.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse httpServletResponse) {
        //1查询最近30天的营业数据
        //最近30天就是从30天之前到昨天
        LocalDate dayBegin = LocalDate.now().minusDays(30);
        LocalDate dayEnd = LocalDate.now().minusDays(1);
        //调用之前工作台的方法获取最近30天的营业数据
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(dayBegin, LocalTime.MIN),
                LocalDateTime.of(dayEnd, LocalTime.MAX));
        //2通过POI写入到excel模板中
        //使用类加载器  从类路径中加载模板文件
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        XSSFWorkbook excel = null;
        ServletOutputStream outputStream = null;
        try {
            //基于模板文件创建一个新的文件
            excel = new XSSFWorkbook(inputStream);
            //开始写入   对着模板找对位置写
            XSSFSheet sheet = excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间范围：" + dayBegin + "至" + dayEnd);
            //概览数据
            sheet.getRow(3).getCell(2).setCellValue(businessData.getTurnover());//营业额
            sheet.getRow(3).getCell(4).setCellValue(businessData.getOrderCompletionRate());//订单完成率
            sheet.getRow(3).getCell(6).setCellValue(businessData.getNewUsers());//新增用户数
            sheet.getRow(4).getCell(2).setCellValue(businessData.getValidOrderCount());//有效订单
            sheet.getRow(4).getCell(4).setCellValue(businessData.getUnitPrice());//平均客单价
            //明细数据   循环写每一天的数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dayBegin.plusDays(i);
                BusinessDataVO dayBusinessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                XSSFRow row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());//日期
                row.getCell(2).setCellValue(dayBusinessData.getTurnover());//营业额
                row.getCell(3).setCellValue(dayBusinessData.getValidOrderCount());//有效订单
                row.getCell(4).setCellValue(dayBusinessData.getOrderCompletionRate());//订单完成率
                row.getCell(5).setCellValue(dayBusinessData.getUnitPrice());//平均客单价
                row.getCell(6).setCellValue(dayBusinessData.getNewUsers());//新增用户
            }
            //3通过输出流在浏览器中下载
            outputStream = httpServletResponse.getOutputStream();
            excel.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            //释放资源  真麻烦
            try {
                excel.close();
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
