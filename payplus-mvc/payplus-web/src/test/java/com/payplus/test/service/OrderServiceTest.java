package com.payplus.test.service;

import com.alibaba.fastjson.JSON;
import com.payplus.domain.req.ShopCartReq;
import com.payplus.domain.res.PayOrderRes;
import com.payplus.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderServiceTest {

    @Resource
    private IOrderService orderService;

    @Test
    public void test() throws Exception {
        ShopCartReq shopCartReq = new ShopCartReq();
        shopCartReq.setUserId("XXR");
        shopCartReq.setProductId("10001");
        PayOrderRes payOrderRes = orderService.createOrder(shopCartReq);
        log.info("请求参数:{}", JSON.toJSONString(shopCartReq));
        log.info("测试结果:{}", JSON.toJSONString(payOrderRes));
    }
}
