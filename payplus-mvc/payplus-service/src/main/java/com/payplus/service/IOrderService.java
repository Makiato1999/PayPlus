package com.payplus.service;

import com.payplus.domain.req.ShopCartReq;
import com.payplus.domain.res.PayOrderRes;

public interface IOrderService {
    PayOrderRes createOrder(ShopCartReq shopCartReq) throws Exception;

}
