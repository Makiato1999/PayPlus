package com.payplus.service.impl;

import com.payplus.common.constants.Constants;
import com.payplus.dao.IOrderDao;
import com.payplus.domain.po.PayOrder;
import com.payplus.domain.req.ShopCartReq;
import com.payplus.domain.res.PayOrderRes;
import com.payplus.domain.vo.ProductVO;
import com.payplus.service.IOrderService;
import com.payplus.service.rpc.ProductRPC;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Random;

/**
 * 订单服务
 */
@Slf4j
@Service
public class OrderServiceImpl implements IOrderService {
    @Resource
    private IOrderDao orderDao;

    @Resource
    private ProductRPC productRPC;

    @Override
    public PayOrderRes createOrder(ShopCartReq shopCartReq) throws Exception {
        // 查询当前用户是否存在未支付订单或者掉单订单
        PayOrder payOrderReq = new PayOrder();
        payOrderReq.setUserId(shopCartReq.getUserId());
        payOrderReq.setProductId(shopCartReq.getProductId());
        PayOrder unpaidOrder = orderDao.queryUnpaidOrder(payOrderReq);

        if (unpaidOrder != null && Constants.OrderStatusEnum.PAY_WAIT.getCode().equals(unpaidOrder.getStatus())) {
            log.info("创建订单-存在，已存在未支付订单。userId:{} productId:{} orderId:{}", shopCartReq.getUserId(), shopCartReq.getProductId(), unpaidOrder.getOrderId());
            return PayOrderRes.builder()
                    .orderId(unpaidOrder.getOrderId())
                    .payUrl(unpaidOrder.getPayUrl())
                    .build();
        } else if (unpaidOrder != null && Constants.OrderStatusEnum.CREATE.getCode().equals(unpaidOrder.getStatus())) {
            log.info("创建订单-存在，存在未创建支付单订单，创建支付单开始 userId:{} productId:{} orderId:{}", shopCartReq.getUserId(), shopCartReq.getProductId(), unpaidOrder.getOrderId());
            // to do
        }

        // 查询商品 & 创建订单 Order
        ProductVO productVO = productRPC.queryProductByProductId(shopCartReq.getProductId());
        String orderId = RandomStringUtils.randomNumeric(16);
        orderDao.insert(PayOrder.builder()
                .userId(shopCartReq.getUserId())
                .productId(shopCartReq.getProductId())
                .productName(productVO.getProductName())
                .orderId(orderId)
                .orderTime(new Date())
                .totalAmount(productVO.getPrice())
                .status(Constants.OrderStatusEnum.CREATE.getCode())
                .build());

        // 创建支付单 todo

        return PayOrderRes.builder()
                .orderId(orderId)
                .payUrl("TBD").build();
    }
}
