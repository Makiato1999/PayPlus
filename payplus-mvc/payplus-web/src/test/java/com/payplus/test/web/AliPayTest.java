package com.payplus.test.web;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;


import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class AliPayTest {
    // 「沙箱环境」应用ID - 您的APPID，收款账号既是你的APPID对应支付宝账号。获取地址；https://open.alipay.com/develop/sandbox/app
    public static String app_id = "9021000143682131";
    // 「沙箱环境」商户私钥，你的PKCS8格式RSA2私钥 - 密钥工具 所创建的公户私钥
    public static String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCDiQDXrSaDSXFXg8BljTron4J24ak2EEdpcvu2JvBMtUBvpQWkGvCqBsVP9TCncpk4yFtcr8gUR+cqHRDT/okGFLSgZlx+KAdP5C5tpzGRvDxb0UnsXpVZz/5KsW2vYxLgQvZ7lqkqIMjrRSLlMy5Lm5fP4AccS+JyxhN4z52Fi33g7A7xOUv/kloB7tvAg8DiTd4sxMmygTqT5WI5GMfHAqQh650R+8t6bPt8dXNKRKUJOBO11+/E41AgHr/9MDGRt5wBfX+bv4uXlzo9a+NjrbjQ26Psn/nRzZSef5tThcPGtCrsxaGWARR6c7ltysowb5Z3pLDIx88ysewgElbhAgMBAAECggEAcQKdwbAXncbU5Z7iB54u1bxuSBub2UJlSJks4JRV82HqycFbtmIbEcodbpQ3yDYypNGnhVvVqG8akllEow9WxO23PvSTVSSpG5/tr/JtKUw0A6MuQzIEWZTVY3FLztttVVbTFN4d238tgM32rDur8/PrnANbuJkOJxQ9mJewPBKgstKjJEJTE0x1isCzwp0mgWTtpXFkBs5vhhFdCDoBvrrhEv/WWbDSLEAIsic4VuWHj+agy0ghYOqe82Sc1YPtRUJlZ2iUu+e2LGxpuPa2JDuaDCaBDvQBRX2FIh1PbDzAp9NK9hlQzaFX8skqhURwA/4aduei6OcxkZvuE4arfQKBgQC35MeA9h+TZkdrzSutqVg9RGChBnUBg0Vd5vjh5AHvq73yAqMCpJIiKy/OQ9z0utZd8+HgkybP+Y5oYpMdEcovZN3iRqOWz1jdG0QCN0xwcn1GwN7ztYDiB7JnvR+W5dNpWt6dt6JXJQjvyWd8X86+37RluZILJVcgc+9Nm1yCYwKBgQC3HHwvIKuwi0qlEMaWbBgzNaHqsCJbEB640vJe0UI1dzNK6/HkBhe4aHwZureNyTZ/wz1Ya/tcbkdDBBYhh60xr3D1GC+7quoAoHCZAhs3OVySmS4LUz7vhNKrXHlkuJFWRIQpOmkZJyNRn8VEJAIMgSx1vXxiyxSmbjUiKQyi6wKBgQCQgmNY75EetxhXGbNCVDq4YryArd9S4gKn6TMZ6KCw5EhvHy1UjVkdKsz1jZTcaNq+mEGfxmOQue70DsrG5Ez7c4t+Hr69a5HMp3mJOEWEdCQIbtaFs9NlTGEhbpidb1v0hek9DaqwSrU8IgyFy5BfpNJRwkOHKL/QC+s5FGbdOwKBgD9FO2+5VTnSV3+DtWrCfPYs4P7Lz4MxpbvtP8wdeEgrIPERZ+qHZGjWyZLGXqhCT2+EEc1cumTswfjZD6CUVfbmDRzTN6Mb4nFG/sEP5kGFs4QAI9XL9OIkPfryIAv6blWlpRjt0uTbDiwlDa+G1aJme+tPBwzH6SG80OU10+TzAoGAGHfSW1P7sccUg43Sb2ldlEVS/0QbQ6U52wVkBZTSt9NDU5ZtW3fp2hnDc8GiUwnabVfTBOuJeXuPjCv313GlTmAaZdadbmhTMHtTpvI4fykhhsnWpwfldq5E+EjnLQZyCzUR8KyYa9A7ANybaOm6ZOk7LS//BudCbH6r7biggPw=";
    // 「沙箱环境」支付宝公钥 - 密钥填写 后提供给你的支付宝公钥
    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq8O5t19LClsSORIN18Z8kORP/8Rhim0+iXXqQXCvDhPS7b1CJaxZ9Nw9zQoCgmOPI22RI5sQqr3mRrbEyNzZtp11617xrIyEx1vaz2fcsg8SE9G5QiaN4kLdv/TdT4t/CrPQxuhfRF0Ezw7eCCZC19I7FstDCsM+6y9MWrwEpSbn1md7b79/myUz8c0g9VWJLYZuTLsyy+mP8DuLp5SOic0kSY9AfPlg7zbwXSg0whTd0qgsoo0S9cmHSZFcjxgSzOGwLgr1UKIzP73YRQpYd3o1b6w94gF9PaHvksMgFxNMENCFZMe/aVAFt45olOlzVkv4VQ/YMTtvQtwxWSc0VwIDAQAB";
    // 「沙箱环境」服务器异步通知回调地址
    public static String notify_url = "http://makiatox-studio.natapp1.cc/api/v1/alipay/alipay_notify_url";
    // 「沙箱环境」页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String return_url = "https://gaga.plus";
    // 「沙箱环境」
    public static String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    // 签名方式
    public static String sign_type = "RSA2";
    // 字符编码格式
    public static String charset = "utf-8";

    private AlipayClient alipayClient;


    @Before
    public void init() {
        this.alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id,
                merchant_private_key,
                "json",
                charset,
                alipay_public_key,
                sign_type);
    }

    @Test
    public void test_aliPay_pageExecute() throws AlipayApiException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();  // 发送请求的 Request类
        request.setNotifyUrl(notify_url);
        request.setReturnUrl(return_url);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", "daniel82AAAA000032333361Y003");  // 我们自己生成的订单编号
        bizContent.put("total_amount", "500000"); // 订单的总金额
        bizContent.put("subject", "小米Su7 Ultra");   // 支付的名称
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");  // 固定配置
        request.setBizContent(bizContent.toString());

        String form = alipayClient.pageExecute(request).getBody();
        log.info("测试结果：{}", form);

        /**
         * 会生成一个form表单；
         */
    }
}
