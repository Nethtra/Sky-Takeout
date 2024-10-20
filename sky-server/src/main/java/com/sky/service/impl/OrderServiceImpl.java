package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 王天一
 * @version 1.0
 */
@Service
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
            orderDetail.setOrderId(orders.getId());//设置关联的订单id
            orderDetailList.add(orderDetail);//添加进集合
        }
        //批量插入order_detail表
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
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单   使用工具类
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
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
        orders.setUserId(BaseContext.getCurrentId());
        List<Orders> ordersList = ordersMapper.select(orders);
        Orders orders1 = ordersList.get(0);
        List<OrderDetail> orderDetailList = orderDetailMapper.selectByOrderId(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders1, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }
}
