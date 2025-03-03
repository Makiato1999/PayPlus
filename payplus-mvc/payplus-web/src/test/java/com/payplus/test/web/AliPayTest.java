package com.payplus.test.web;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AliPayTest {

    @Test
    public void testAliPay() {
        InetAddress address = null;
        try {
            address = InetAddress.getByName("openapi.alipay.com");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Alipay IP: " + address.getHostAddress());
    }
}
