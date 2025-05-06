package com.payplus.domain.payment.service;

import com.alipay.api.AlipayApiException;
import com.payplus.domain.order.model.entity.PayOrderEntity;
import com.payplus.domain.payment.adapter.port.IPaymentPort;
import com.payplus.domain.payment.model.valobj.PrepayCommand;
import com.payplus.domain.payment.service.strategy.PaymentStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentService implements IPaymentService {
    private final PaymentStrategyFactory strategyFactory;

    public PaymentService(PaymentStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    @Override
    public PayOrderEntity prepay(PrepayCommand command) throws AlipayApiException {
        IPaymentPort port = strategyFactory.getPort(command.getPayMethod());
        /*
        port 是一个 IPaymentPort 接口类型；
        但 strategyFactory.getPort(...) 返回的是你注入到 Map<String, IPaymentPort> 中的具体实现类

        在 PaymentService 中只依赖于抽象接口 IPaymentPort；
        具体使用哪个支付方式，由 PaymentStrategyFactory 根据 payMethod 决定；
        这样你可以灵活扩展新的支付方式，只要实现 IPaymentPort 并注册为 Spring Bean 即可，无需修改原有代码

        port 实际是指向 AlipayPort 的对象引用。
        Spring 知道 AlipayPort 是 IPaymentPort 的实现类，因此它会实例化 AlipayPort 并注入。
        你的代码永远依赖的是接口，实际运行的是实现类
         */
        return port.prepay(command);
    }

}

