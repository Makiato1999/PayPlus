package com.payplus.domain.payment.adapter.port;

import com.alipay.api.AlipayApiException;
import com.payplus.domain.order.model.entity.PayOrderEntity;
import com.payplus.domain.payment.model.valobj.PrepayCommand;

public interface IPaymentPort {
    String getPayMethod();
    /**
     * 发起支付请求，生成支付链接或二维码。
     * @param command 支付命令参数（订单 ID、金额、商品名等）
     * @return 支付订单信息（包含支付链接、状态等）
     */
    PayOrderEntity prepay(PrepayCommand command) throws AlipayApiException;
}
