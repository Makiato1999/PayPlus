package com.payplus.controller;

import com.payplus.common.wechat.WeChatSignatureUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController()
public class WeChatPortalController {

    @Value("${wechat.config.originalid}")
    private String originalid;
    @Value("${wechat.config.token}")
    private String token;

    @RequestMapping(value = "/receive", method = RequestMethod.GET, produces = "text/plain;charset=utf-8")
    public String validate(@RequestParam(value = "signature") String signature,
                           @RequestParam(value = "timestamp") String timestamp,
                           @RequestParam(value = "nonce") String nonce,
                           @RequestParam(value = "echostr") String echostr) {
        try {
            log.info("WeChat 公众号 signature verification started [{}, {}, {}, {}]",
                    signature, timestamp, nonce, echostr);

            if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
                throw new IllegalArgumentException("Request parameters are illegal");
            }

            boolean verifyStatus = WeChatSignatureUtil.verify(token, signature, timestamp, nonce);
            log.info("WeChat 公众号 signature verification done, verify status：{}", verifyStatus);
            if (verifyStatus) {
                return echostr;
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("WeChat 公众号 signature verification failed [{}, {}, {}, {}]",
                    signature, timestamp, nonce, echostr);
            return null;
        }
    }

    @RequestMapping(value = "/receive", method = RequestMethod.POST, produces = "application/xml; charset=UTF-8")
    public String
}
