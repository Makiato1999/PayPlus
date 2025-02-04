package com.payplus.service.impl;

import com.payplus.dao.IOrderDao;
import com.payplus.domain.po.PayOrder;
import com.payplus.domain.req.ShopCartReq;
import com.payplus.domain.res.PayOrderRes;
import com.payplus.service.IOrderService;

import javax.annotation.Resource;

/**
 * 订单服务
 */
public class OrderServiceImpl implements IOrderService {
    @Resource
    private IOrderDao orderDao;
    @Override
    public PayOrderRes createOrder(ShopCartReq shopCartReq) throws Exception {
        // 查询当前用户是否存在未支付订单或者掉单订单
        PayOrder payOrderReq = new PayOrder();
        payOrderReq.setUserId(shopCartReq.getUserId());
        payOrderReq.setProductId(shopCartReq.getProductId());
        PayOrder unpaidOrder = orderDao.queryUnpaid
        return null;
    }
}
