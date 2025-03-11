package com.sky.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WebSocketServer webSocketServer;

    @Override
    @Transactional//事务
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //操作两张表  orders  和  order_detail
        //1先用DTO的数据检查 检查地址和购物车是否为空  空就不能下单   所以还要查询address_book和shopping_cart两张表
        AddressBook address = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (address == null)
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);

        Long userId = BaseContext.getCurrentId();//注意根据mapper中方法的要求来
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.select(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.size() == 0)
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);

        //2向orders表中插入一条数据
        //先设置好所有的属性   仔细对比DTO中没有传过来的  然后设置
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);//已经有的直接拷贝
        orders.setNumber(String.valueOf(System.currentTimeMillis()));//用时间戳来设置订单号
        orders.setStatus(Orders.PENDING_PAYMENT);//待付款
        orders.setUserId(userId);//上面检查的时候就拿出的userID
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);//未支付
        orders.setPhone(address.getPhone());//这里还是用上面就拿到的address
        orders.setAddress(address.getDetail());
        orders.setConsignee(address.getConsignee());

        //注意这里要拿回主键id来给下面order_detail关联用
        ordersMapper.insert(orders);

        //3向order_detail表中插入n条数据
        //用户最终下单的商品要从购物车获取
        List<OrderDetail> orderDetailList = new ArrayList<>();
        //构造OrderDetail对象
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());//设置order_detail关联的订单id
            orderDetailList.add(orderDetail);//添加进集合
        }
        //批量插入到order_detail表
        orderDetailMapper.insertBatch(orderDetailList);

        //4然后清空用户购物车的数据
        shoppingCartMapper.delete(shoppingCart);

        //5构造VO 然后返回
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    //绕过微信支付
    //先梳理一遍流程，第一次点支付按钮  小程序请求/payment  然后在这里请求微信的接口  返回预支付标识
    //输入密码后 小程序直接拿着预支付标识请求微信后端  微信后端返回sucess   并调起外卖后端/paySuccess  然后在下面的paySuccess更新订单信息
    //现在直接修改微信前端的代码  输入密码后直接重定向到支付成功页面
    //后端这里注释掉调用微信支付的接口 用到退款的地方也别用了  直接返回空的JSONobject  错了 不能是空的 要随便加一个prepay_id骗前端
    //然后因为还没调/paySuccess  就直接在payment里调一下
    //前端点击去支付  请求后端/payment   后端这里注释掉调用微信支付的接口  返回一个假的OrderPaymentVO 并调用下面的paySuccess直接模拟微信回调 更新数据
    //前端那里改代码，点击确认支付直接重定向到成功页面
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单   使用工具类
        /*JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );*/
        JSONObject jsonObject = new JSONObject();//1
        jsonObject.put("prepay_id", "111");//1   填一个假的prepay_id

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
        paySuccess(ordersPaymentDTO.getOrderNumber());//1  调paySuccess模拟微信回调我们后端来更新订单数据
        return vo;
    }

    /**
     * 支付成功，修改订单状态    微信回调此方法  其实应该回调的是PayNotifyController中的那个方法
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = ordersMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        ordersMapper.update(orders);
        //36支付成功后向管理端商家浏览器推送提醒
        Map map = new HashMap();//通过map构造json字符串
        map.put("type", 1);//1来单提醒
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号：" + outTradeNo);
        String jsonString = JSON.toJSONString(map);
        //注入WebSocketServer  然后调用向客户端发送消息的方法
        //注意因为约定数据格式为json且字段也有约定（type orderId content）   所以要在上面先转成json 构造json字符串
        webSocketServer.sendToAllClient(jsonString);
    }

    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        //看返回数据还需要有订单明细   所以分开两张表查  涉及到orders和order_detail两张表
        //先查orders封装成Page
        //然后查order_detail  set到Page中的OrderVO的orderDetailList字段
        //不要搞连接查询  封装不上的
        //OrderVO extends Orders
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());//设置用户id
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = ordersMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> orderVOList = new ArrayList<>();
        if (page != null && page.getTotal() > 0) {
            for (Orders order : page) {
                //查询order_detail
                List<OrderDetail> orderDetailList = orderDetailMapper.selectByOrderId(order.getId());
                //构造VO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                orderVOList.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), orderVOList);
    }

    @Override
    public OrderVO orderDetail(Long id) {
        //先查寻orders表  然后查order_detail表  封装成VO
        Orders orders = new Orders();
        orders.setId(id);
//        orders.setUserId(BaseContext.getCurrentId());
        List<Orders> ordersList = ordersMapper.select(orders);
        Orders orders1 = ordersList.get(0);
        List<OrderDetail> orderDetailList = orderDetailMapper.selectByOrderId(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders1, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    @Override
    public void cancelOrder(Long id) throws Exception {
        //分析业务规则
        //取消时先检查当前的订单状态
        //1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        //注意Orders里有一个status 还有一个payStatus
//        待支付和待接单状态下，用户可直接取消订单
//        如果在待接单状态下取消订单，需要给用户退款
//        商家已接单和配送中状态下，用户取消订单需电话沟通商家
//        取消订单后需要将订单状态修改为“已取消”
        Orders order = ordersMapper.selectById(id);
        if (order == null)//检查订单是否存在
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        else if (order.getStatus() > 2)//>2不给取消
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR + "请联系骑手或商家");
        else if (order.getStatus().equals(Orders.TO_BE_CONFIRMED)) {//状态为2退款
            //调用微信支付退款接口
//            weChatPayUtil.refund(
//                    order.getNumber(), //商户订单号
//                    order.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额
            //payStatus状态修改为 已退款
            log.info("取消订单，退款");
            order.setPayStatus(Orders.REFUND);
        }
        //如果是1的话就直接走这里  就是少一步退款
        order.setStatus(Orders.CANCELLED);//最后设置状态为已取消
        ordersMapper.update(order);
    }

    @Override
    public void repetition(Long id) {
        //逻辑：将这次订单的所有商品重新加入购物车
        //只有order_detail中还有信息
        //将List中的 OrderDetail对象转换为ShoppingCart对象  然后插入shopping_cart表
        List<OrderDetail> orderDetailList = orderDetailMapper.selectByOrderId(id);
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(orderDetail -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart, "id");//排除属性id
            shoppingCart.setUserId(BaseContext.getCurrentId());//补充一些属性
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        shoppingCartMapper.insertBatch(shoppingCartList);//批量插入购物车
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        //返回数据中要求orderDishes   所以要使用OrderVO
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = ordersMapper.pageQuery(ordersPageQueryDTO);
        List<Orders> ordersList = page.getResult();
        List<OrderVO> orderVOList = new ArrayList<>();
        for (Orders order : ordersList) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            orderVO.setOrderDishes(getOrderDishes(order));//考虑如何拼出菜品的字符串
            orderVOList.add(orderVO);
        }
        return new PageResult(page.getTotal(), orderVOList);
    }


    /**
     * 根据订单id获取该订单用字符串形式拼接的菜品
     *
     * @param orders
     * @return
     */
    private String getOrderDishes(Orders orders) {
        //查出订单明细
        List<OrderDetail> orderDetailList = orderDetailMapper.selectByOrderId(orders.getId());
        //然后转成字符串集合
        List<String> orderDishes = orderDetailList.stream().map(orderDetail -> {
            String DishStr = orderDetail.getName() + "*" + orderDetail.getNumber() + ";";
            return DishStr;
        }).collect(Collectors.toList());
        //String.join字符串拼接 前面是分隔符 后面是要拼接的字符串
        //表示将字符串集合里的所有字符串拼接成一个然后再返回
        return String.join("", orderDishes);
    }

    @Override
    public OrderStatisticsVO countStatistics() {
        Integer toBeConfirmed = ordersMapper.countOrdersByStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = ordersMapper.countOrdersByStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = ordersMapper.countOrdersByStatus(Orders.DELIVERY_IN_PROGRESS);
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        //更改订单状态为已接单
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        ordersMapper.update(orders);
    }

    @Transactional
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        //只有订单状态是待接单时才能拒单
        Orders orders = ordersMapper.selectById(ordersRejectionDTO.getId());
        if (!orders.getStatus().equals(Orders.TO_BE_CONFIRMED))
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        //修改订单状态为已取消，然后填写拒单原因
        orders.setCancelReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        orders.setStatus(Orders.CANCELLED);
        ordersMapper.update(orders);
        //然后退款
        orders = ordersMapper.selectById(ordersRejectionDTO.getId());
//        weChatPayUtil.refund(orders.getNumber(), orders.getNumber(), new BigDecimal(0.01), new BigDecimal(0.01));
        log.info("商家拒单，退款");
        orders.setPayStatus(Orders.REFUND);//更新支付状态
        ordersMapper.update(orders);
    }

    @Transactional
    @Override
    public void merchantCancelOrder(OrdersCancelDTO ordersCancelDTO) {
        //感觉和拒单基本一样
        //但是看前端发现是 拒单只能在待接单状态拒 取消在各种状态都可以
        //所以要加个判断 如果已付款就要退钱
        Orders order = ordersMapper.selectById(ordersCancelDTO.getId());

        order.setStatus(Orders.CANCELLED);
        order.setCancelReason(ordersCancelDTO.getCancelReason());
        order.setCancelTime(LocalDateTime.now());
        ordersMapper.update(order);

        if (order.getPayStatus().equals(Orders.PAID)) {
            log.info("商家取消订单，退款");
            order.setPayStatus(Orders.REFUND);
            ordersMapper.update(order);
        }
    }

    @Override
    public void deliveryOrder(Long id) {
        //将订单状态改为派送中  且只能由已接单状态转变
        Orders order = ordersMapper.selectById(id);
        if (!(order.getStatus().equals(Orders.CONFIRMED)))
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        order.setStatus(Orders.DELIVERY_IN_PROGRESS);
        ordersMapper.update(order);
    }

    @Override
    public void completeOrder(Long id) {
        //只能将配送中的订单改为已完成状态
        Orders order = ordersMapper.selectById(id);
        if (!(order.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)))
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        order.setStatus(Orders.COMPLETED);
        ordersMapper.update(order);
    }

    @Override
    public void reminder(Long id) {
        //逻辑和来单提醒基本一样
        Orders order = ordersMapper.selectById(id);
        if (order == null)
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        Map map = new HashMap();
        map.put("type", 2);
        map.put("orderId", order.getId());
        map.put("content", "订单号：" + order.getNumber());
        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }
}
