package com.payplus.domain.order.adapter.repository;

import com.payplus.domain.order.model.aggregate.CreateOrderAggregate;
import com.payplus.domain.order.model.entity.OrderEntity;
import com.payplus.domain.order.model.entity.PayOrderEntity;
import com.payplus.domain.order.model.entity.ShopCartEntity;

import java.util.List;

public interface IOrderRepository {
    void doSaveOrder(CreateOrderAggregate orderAggregate);

    OrderEntity queryUnpaidOrder(ShopCartEntity shopCartEntity);

    void updateOrderPayInfo(PayOrderEntity payOrderEntity);

    boolean changeOrderPaySuccess(String orderId);

    List<String> queryNoPayNotifyOrder();

    List<String> queryTimeoutCloseOrderList();

    boolean changeOrderClose(String orderId);
}
