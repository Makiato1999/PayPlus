package com.payplus.service.impl;

import com.payplus.domain.req.WeChatQrCodeReq;
import com.payplus.domain.res.WeChatQrCodeRes;
import com.payplus.domain.res.WeChatTokenRes;
import com.payplus.service.wechat.IWeChatApiService;
import org.springframework.web.client.RestTemplate;

public class WeChatApiServiceImpl implements IWeChatApiService {
    private static final String BASE_URL = "https://api.weixin.qq.com";
    private final RestTemplate restTemplate;

    public WeChatApiServiceImpl(RestTemplate restTemplate) { this.restTemplate = restTemplate; }
    @Override
    public WeChatTokenRes getToken(String grantType, String appId, String appSecret) {
        String url = String.format(
                "%s/cgi-bin/token?grant_type=%s&appid=%s&secret=%s",
                BASE_URL,
                grantType, appId, appSecret);

        // GET request
        WeChatTokenRes res = restTemplate.getForObject(url, WeChatTokenRes.class);
        if (res == null) {
            throw new RuntimeException("Token is null");
        }
        if (res.getErrcode() != 0) {
            throw new RuntimeException(String.format("Token request failed, errcode = %d, errmsg = %s",
                    res.getErrcode(), res.getErrmsg()));
        }
        return res;
    }

    @Override
    public WeChatQrCodeRes getToken(String token, WeChatQrCodeReq weChatQrCodeReq) {
        String url = String.format("%s/cgi-bin/qrcode/create?access_token=%s",
                BASE_URL, token);

        // POST request
        WeChatQrCodeRes res = restTemplate.postForObject(url, weChatQrCodeReq, WeChatQrCodeRes.class);
        if (res == null) {
            throw new RuntimeException("QrCode is null");
        }
        return res;
    }
}
