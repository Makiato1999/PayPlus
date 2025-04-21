package com.payplus.job;

import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.payplus.service.impl.OrderServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 这个类是一个支付宝支付状态兜底定时任务：
 * 每 3 秒扫一次数据库中“支付中”的订单，调用支付宝接口验证真实支付状态，如果已付款就手动更新系统里的订单状态
 */
@Slf4j
@Component
public class NoPayNotifyOrderJob {
    @Resource
    private OrderServiceImpl orderService;

    @Resource
    private AlipayClient alipayClient;

    /**
     * 每 3 秒执行一次（0/3 表示每 3 秒触发一次任务）。
     * 适用于高频支付检查，防止订单状态丢失。
     */
    @Scheduled(cron = "0/3 * * * * ?")
    public void exec() {
        try {
            log.info("任务: 检测未接收到或未正确处理的支付回调通知");
            /*
            查的是超过 1 分钟未支付的订单，但它们可能已经支付了，只是我们的数据库没更新。
             */
            List<String> orderIds = orderService.queryNoPayNotifyOrder();
            /*
            然后去支付宝查询这些订单的真实支付状态：
            如果支付宝显示支付成功了，但我们的数据库还没有更新，就手动更新订单状态为 PAID
             */
            if (null == orderIds || orderIds.isEmpty()) return;

            for (String orderId : orderIds) {
                AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
                AlipayTradeQueryModel bizModel = new AlipayTradeQueryModel();
                bizModel.setOutTradeNo(orderId);
                request.setBizModel(bizModel);

                AlipayTradeQueryResponse alipayTradeQueryResponse = alipayClient.execute(request);
                String code = alipayTradeQueryResponse.getCode();

                if ("40004".equals(code) && "ACQ.TRADE_NOT_EXIST".equals(alipayTradeQueryResponse.getSubCode())) {
                    log.warn("订单 {} 在支付宝不存在，可能尚未支付或订单号错误", orderId);
                    continue;
                }

                // 判断状态码，code == 10000：表示支付宝查询成功，说明订单已付款
                if ("10000".equals(code)) {
                    // 把订单手动改为已支付（更新数据库）
                    orderService.changeOrderPaySuccess(orderId);
                }
            }


        } catch (Exception e) {
            log.error("检测未接收到或未正确处理的支付回调通知 失败", e);
        }
    }
}
