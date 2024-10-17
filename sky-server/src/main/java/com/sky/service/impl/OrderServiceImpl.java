package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
