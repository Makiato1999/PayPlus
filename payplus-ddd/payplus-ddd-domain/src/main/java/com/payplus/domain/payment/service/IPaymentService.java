package com.payplus.domain.payment.service;

import com.alipay.api.AlipayApiException;
import com.payplus.domain.order.model.entity.PayOrderEntity;
import com.payplus.domain.payment.model.valobj.PrepayCommand;

public interface IPaymentService {
    PayOrderEntity prepay(PrepayCommand command) throws AlipayApiException;
}
