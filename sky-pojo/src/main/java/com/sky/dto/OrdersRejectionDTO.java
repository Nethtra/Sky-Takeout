package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrdersRejectionDTO implements Serializable {
    //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
    private Long id;

    //订单拒绝原因
    private String rejectionReason;

}
