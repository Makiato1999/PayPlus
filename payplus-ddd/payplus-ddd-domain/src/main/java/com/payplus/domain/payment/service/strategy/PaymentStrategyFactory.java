package com.payplus.domain.payment.service.strategy;

import com.payplus.domain.payment.adapter.port.IPaymentPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class PaymentStrategyFactory {
    private final Map<String, IPaymentPort> portMap = new HashMap<>();

    public PaymentStrategyFactory(List<IPaymentPort> ports) {
        for (IPaymentPort port : ports) {
            log.info("注册支付策略：{}", port.getPayMethod());
            portMap.put(port.getPayMethod(), port);
        }
        /*
        List<IPaymentPort> 是个接口类型列表。
        Spring 会查找所有 实现了 IPaymentPort 接口的 Bean（比如 AlipayPort），然后自动装配进 ports。
        所以，虽然你代码中拿的是接口 IPaymentPort，但实际上 ports 中每一个对象，都是 AlipayPort 实例（或将来你加的其他实现类，比如 WechatPort）
         */
    }

    public IPaymentPort getPort(String payMethod) {
        IPaymentPort port = portMap.get(payMethod);
        if (port == null) {
            throw new IllegalArgumentException("不支持的支付方式：" + payMethod);
        }
        return port;
    }
}
