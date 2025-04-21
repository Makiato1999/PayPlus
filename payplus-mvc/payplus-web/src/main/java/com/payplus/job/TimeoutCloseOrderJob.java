package com.payplus.job;

import com.payplus.service.impl.OrderServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 这个类，用于定期清理“未支付订单”的定时任务，每 10 分钟执行一次，将超过 30 分钟未支付的订单设置为“已关闭”状态
 */
@Slf4j
@Component()
public class TimeoutCloseOrderJob {
    @Resource
    private OrderServiceImpl orderService;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void exec() {
        try {
            log.info("任务；超时30分钟订单关闭");
            List<String> orderIds = orderService.queryTimeoutCloseOrderList();
            if (null == orderIds || orderIds.isEmpty()) {
                log.info("定时任务，超时30分钟订单关闭，暂无超时未支付订单 orderIds is null");
                return;// 没有超时订单就直接跳过
            }
            for (String orderId : orderIds) {
                boolean status = orderService.changeOrderClose(orderId);
                log.info("定时任务，超时30分钟订单关闭 orderId: {} status：{}", orderId, status);
            }
        } catch (Exception e) {
            log.error("定时任务，超时30分钟订单关闭失败", e);
        }
    }

}
