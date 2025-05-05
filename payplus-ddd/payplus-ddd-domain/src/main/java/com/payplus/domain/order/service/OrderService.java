package com.payplus.domain.order.service;

import com.alipay.api.AlipayApiException;
import com.payplus.domain.order.adapter.port.IProductPort;
import com.payplus.domain.order.adapter.repository.IOrderRepository;
import com.payplus.domain.order.model.aggregate.CreateOrderAggregate;
import com.payplus.domain.order.model.entity.PayOrderEntity;
import com.payplus.domain.payment.adapter.port.IPaymentPort;
import com.payplus.domain.payment.model.valobj.PrepayCommand;
import com.payplus.domain.payment.service.IPaymentService;
import com.payplus.domain.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class OrderService extends AbstractOrderService {
    public OrderService(IOrderRepository repository, IProductPort port, PaymentService paymentService) {
        super(repository, port, paymentService);
    }

    @Override
    protected void doSaveOrder(CreateOrderAggregate orderAggregate) {
        repository.doSaveOrder(orderAggregate);
    }

    @Override
    protected PayOrderEntity doPrepayOrder(String productId, String productName, String orderId, BigDecimal price) throws AlipayApiException {
        PrepayCommand prepayCommand = PrepayCommand.builder()
                .productId(productId)
                .productName(productName)
                .orderId(orderId)
                .amount(price)
                .payMethod("ALIPAY").build();

        // 调用支付服务，获取支付结果（包含 payUrl）
        PayOrderEntity payOrderEntity = paymentService.prepay(prepayCommand);
        /*
        Q: OrderService 里也没有调用工厂的构造器，为什么它还是能工作?
        A: 因为用了 Spring 的自动依赖注入机制（IoC 容器），无需自己显式调用构造器
            PaymentStrategyFactory 被 @Component 标注了，Spring 会自动创建它，并注入所有 IPaymentPort 的实现
            Spring详细流程：
            扫描到 @Service 的 AlipayPort，注册为 Bean。
            扫描到 @Component 的 PaymentStrategyFactory，发现它依赖 List<IPaymentPort>。
            找到所有的 IPaymentPort 实现类（此处只有 AlipayPort），注入进去。
            构造 PaymentStrategyFactory 时自动完成 Map<String, IPaymentPort> 的初始化
         */

        // 更新订单中的支付信息
        repository.updateOrderPayInfo(payOrderEntity);
        return payOrderEntity;
    }
}
