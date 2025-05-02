package com.payplus.domain.order.service;

import com.payplus.domain.order.adapter.port.IProductPort;
import com.payplus.domain.order.adapter.repository.IOrderRepository;
import com.payplus.domain.order.model.aggregate.CreateOrderAggregate;
import com.payplus.domain.order.model.entity.OrderEntity;
import com.payplus.domain.order.model.entity.PayOrderEntity;
import com.payplus.domain.order.model.entity.ProductEntity;
import com.payplus.domain.order.model.entity.ShopCartEntity;
import com.payplus.domain.order.model.valobj.OrderStatusVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractOrderService implements IOrderService {
    protected final IOrderRepository repository;

    protected final IProductPort port;

    public AbstractOrderService(IOrderRepository repository, IProductPort port) {
        this.repository = repository;
        this.port = port;
    }

    @Override
    public PayOrderEntity createOrder(ShopCartEntity shopCartEntity) throws Exception {
        // 查询当前用户是否存在未支付订单或者掉单订单
        OrderEntity unpaidOrderEntity = repository.queryUnpaidOrder(shopCartEntity);

        if (unpaidOrderEntity != null && OrderStatusVO.PAY_WAIT.getCode().equals(unpaidOrderEntity.getOrderStatusVO())) {
            log.info("创建订单-存在，已存在未支付订单。userId:{} productId:{} orderId:{}", shopCartEntity.getUserId(), shopCartEntity.getProductId(), unpaidOrderEntity.getOrderId());
            return PayOrderEntity.builder()
                    .orderId(unpaidOrderEntity.getOrderId())
                    .payUrl(unpaidOrderEntity.getPayUrl())
                    .build();
        } else if (unpaidOrderEntity != null && OrderStatusVO.CREATE.getCode().equals(unpaidOrderEntity.getOrderStatusVO())) {
//            log.info("创建订单-存在，存在未创建支付单订单，创建支付单开始 userId:{} productId:{} orderId:{}", shopCartReq.getUserId(), shopCartReq.getProductId(), unpaidOrder.getOrderId());
//            PayOrder payOrder = doPrepayOrder(unpaidOrder.getProductId(), unpaidOrder.getProductName(), unpaidOrder.getOrderId(), unpaidOrder.getTotalAmount());
//            return PayOrderRes.builder()
//                    .orderId(payOrder.getOrderId())
//                    .payUrl(payOrder.getPayUrl())
//                    .build();
        }

        ProductEntity productEntity = port.queryProductByProductId(shopCartEntity.getProductId());

        OrderEntity orderEntity = CreateOrderAggregate.buildOrderEntity(productEntity.getProductId(), productEntity.getProductName());

        CreateOrderAggregate orderAggregate = CreateOrderAggregate.builder()
                .userId(shopCartEntity.getUserId())
                .productEntity(productEntity)
                .orderEntity(orderEntity)
                .build();

        this.doSaveOrder(orderAggregate);

        return PayOrderEntity.builder()
                .orderId(orderEntity.getOrderId())
                .payUrl("暂无").build();
    }

    /**
     * 保存订单
     *
     * @param orderAggregate 订单聚合
     */
    protected abstract void doSaveOrder(CreateOrderAggregate orderAggregate);

}
