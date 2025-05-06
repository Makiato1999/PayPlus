package com.payplus.trigger.listener;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 支付成功回调消息
 */
@Slf4j
@Component
public class OrderPaySuccessListener {

    @Subscribe
    public void handleEvent(String paySuccessMessage) {
        log.info("收到支付成功消息，可以做接下来的事情了【发货、充值、开会员】paySuccessMessage：{}", paySuccessMessage);
    }

}

