package com.payplus.infrastructure.adapter.port;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.payplus.domain.order.model.entity.PayOrderEntity;
import com.payplus.domain.order.model.valobj.OrderStatusVO;
import com.payplus.domain.payment.adapter.port.IPaymentPort;
import com.payplus.domain.payment.model.valobj.PrepayCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AlipayPort implements IPaymentPort {
    /*
    这段代码告诉 Spring：注册一个名为 alipayPort 的 Bean，实现了接口 IPaymentPort。
    Spring 容器启动时，会找到所有标注了 @Service, @Component 的类，将它们 实例化为对象 并注册进 IOC 容器中。
    此时 AlipayPort 的实例，是一个 IPaymentPort 类型的 Bean
    */
    @Value("${alipay.notify_url}")
    private String notifyUrl;

    @Value("${alipay.return_url}")
    private String returnUrl;

    private final AlipayClient alipayClient;

    public AlipayPort(AlipayClient alipayClient) {
        this.alipayClient = alipayClient;
    }

    @Override
    public String getPayMethod() {
        return "ALIPAY";
    }

    @Override
    public PayOrderEntity prepay(PrepayCommand command) throws AlipayApiException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", command.getOrderId());
        bizContent.put("total_amount", command.getAmount().toString());
        bizContent.put("subject", command.getProductName());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());

        String form = alipayClient.pageExecute(request).getBody();

        return PayOrderEntity.builder()
                .orderId(command.getOrderId())
                .payUrl(form)
                .orderStatusVO(OrderStatusVO.PAY_WAIT).build();
    }
}
