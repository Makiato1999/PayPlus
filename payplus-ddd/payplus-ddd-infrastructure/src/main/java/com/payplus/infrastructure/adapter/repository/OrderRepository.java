package com.payplus.infrastructure.adapter.repository;

import com.google.common.eventbus.EventBus;
import com.payplus.domain.order.adapter.event.PaySuccessMessageEvent;
import com.payplus.domain.order.adapter.repository.IOrderRepository;
import com.payplus.domain.order.model.aggregate.CreateOrderAggregate;
import com.payplus.domain.order.model.entity.OrderEntity;
import com.payplus.domain.order.model.entity.PayOrderEntity;
import com.payplus.domain.order.model.entity.ProductEntity;
import com.payplus.domain.order.model.entity.ShopCartEntity;
import com.payplus.domain.order.model.valobj.OrderStatusVO;
import com.payplus.infrastructure.dao.IOrderDao;
import com.payplus.infrastructure.dao.po.PayOrder;
import com.payplus.types.event.BaseEvent;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class OrderRepository implements IOrderRepository {
    @Resource
    private IOrderDao orderDao;

    @Resource
    private PaySuccessMessageEvent paySuccessMessageEvent;

    @Resource
    private EventBus eventBus;

    @Override
    public void doSaveOrder(CreateOrderAggregate orderAggregate) {
        String userId = orderAggregate.getUserId();
        ProductEntity productEntity = orderAggregate.getProductEntity();
        OrderEntity orderEntity = orderAggregate.getOrderEntity();

        PayOrder order = new PayOrder();
        order.setUserId(userId);
        order.setProductId(productEntity.getProductId());
        order.setProductName(productEntity.getProductName());
        order.setOrderId(orderEntity.getOrderId());
        order.setOrderTime(orderEntity.getOrderTime());
        order.setTotalAmount(productEntity.getPrice());
        order.setStatus(orderEntity.getOrderStatusVO().getCode());

        orderDao.insert(order);
    }

    @Override
    public OrderEntity queryUnpaidOrder(ShopCartEntity shopCartEntity) {
        // 1. 封装参数
        PayOrder orderReq = new PayOrder();
        orderReq.setUserId(shopCartEntity.getUserId());
        orderReq.setProductId(shopCartEntity.getProductId());
        // 2. 查询到订单
        PayOrder order = orderDao.queryUnpaidOrder(orderReq);
        if (null == order) return null;

        // 3. 返回结果
        return OrderEntity.builder()
                .productId(order.getProductId())
                .productName(order.getProductName())
                .orderId(order.getOrderId())
                .orderStatusVO(OrderStatusVO.valueOf(order.getStatus()))
                .orderTime(order.getOrderTime())
                .totalAmount(order.getTotalAmount())
                .payUrl(order.getPayUrl())
                .build();
    }

    @Override
    public void updateOrderPayInfo(PayOrderEntity payOrderEntity) {
        PayOrder payOrder = PayOrder.builder()
                .userId(payOrderEntity.getUserId())
                .orderId(payOrderEntity.getOrderId())
                .status(payOrderEntity.getOrderStatusVO().getCode())
                .payUrl(payOrderEntity.getPayUrl())
                .build();
        orderDao.updateOrderPayInfo(payOrder);
    }

    @Override
    public boolean changeOrderPaySuccess(String orderId) {
        PayOrder payOrder = PayOrder.builder()
                .orderId(orderId)
                .status(OrderStatusVO.PAY_SUCCESS.getCode()).build();
        orderDao.changeOrderPaySuccess(payOrder);

        BaseEvent.EventMessage<PaySuccessMessageEvent.PaySuccessMessage> paySuccessMessageEventMessage = paySuccessMessageEvent.buildEventMessage(PaySuccessMessageEvent.PaySuccessMessage.builder()
                .tradeNo(orderId).build());
        PaySuccessMessageEvent.PaySuccessMessage paySuccessMessage = paySuccessMessageEventMessage.getData();

        eventBus.post(paySuccessMessage);
        return false;
    }

    @Override
    public List<String> queryNoPayNotifyOrder() {
        return orderDao.queryNoPayNotifyOrder();
    }

    @Override
    public List<String> queryTimeoutCloseOrderList() {
        return orderDao.queryTimeoutCloseOrderList();
    }

    @Override
    public boolean changeOrderClose(String orderId) {
        return orderDao.changeOrderClose(orderId);
    }

}
