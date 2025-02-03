package com.payplus.service.impl;

import com.payplus.domain.req.WeChatQrCodeReq;
import com.payplus.domain.res.WeChatQrCodeRes;
import com.payplus.domain.res.WeChatTokenRes;
import com.payplus.domain.vo.WeChatTemplateMessageVO;
import com.payplus.service.wechat.IWeChatApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class WeChatApiServiceImpl implements IWeChatApiService {
    private static final String BASE_URL = "https://api.weixin.qq.com";

    @Autowired
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

        return res;
    }

    @Override
    public WeChatQrCodeRes createQrCode(String token, WeChatQrCodeReq weChatQrCodeReq) {
        String url = String.format("%s/cgi-bin/qrcode/create?access_token=%s",
                BASE_URL, token);

        // POST request
        WeChatQrCodeRes res = restTemplate.postForObject(url, weChatQrCodeReq, WeChatQrCodeRes.class);

        return res;
    }

    @Override
    public void sendMessage(String accessToken, WeChatTemplateMessageVO wechatTemplateMessageVO) {
        // 微信发送模板消息的接口地址（注意：access_token 要通过参数传递）
        String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accessToken;

        // 创建 RestTemplate 实例（也可以通过 Spring 注入）
        RestTemplate restTemplate = new RestTemplate();

        // 设置请求头，指定传输格式为 JSON
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 构造请求体
        HttpEntity<WeChatTemplateMessageVO> requestEntity = new HttpEntity<>(wechatTemplateMessageVO, headers);

        try {
            // 发送 POST 请求
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info("微信模板消息发送成功，响应内容：{}", responseEntity.getBody());
            } else {
                log.error("微信模板消息发送失败，状态码：{}，响应内容：{}", responseEntity.getStatusCode(), responseEntity.getBody());
            }
        } catch (Exception e) {
            log.error("调用微信模板消息接口异常", e);
            throw new RuntimeException("发送微信模板消息异常", e);
        }
    }
}
