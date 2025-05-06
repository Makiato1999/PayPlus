package com.payplus.domain.order.service;

import com.payplus.domain.order.model.entity.PayOrderEntity;
import com.payplus.domain.order.model.entity.ShopCartEntity;

import java.util.List;

public interface IOrderService {
    /**
     * 通过购物车实体对象，创建支付单实体（用于支付）—— 所有的订单下单都从购物车开始触发
     *
     * @param shopCartEntity 购物车实体
     * @return 支付单实体
     */
    PayOrderEntity createOrder(ShopCartEntity shopCartEntity) throws Exception;

    /**
     * 更新订单状态
     * @param orderId 订单ID
     */
    void changeOrderPaySuccess(String orderId);

    /**
     * 查询有效期内，未接收到支付回调的订单
     */
    List<String> queryNoPayNotifyOrder();

    /**
     * 查询超时15分钟，未支付订单
     */
    List<String> queryTimeoutCloseOrderList();

    boolean changeOrderClose(String orderId);

}
