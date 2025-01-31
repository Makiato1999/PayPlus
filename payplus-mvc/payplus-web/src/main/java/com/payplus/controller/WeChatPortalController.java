package com.payplus.controller;

import com.payplus.common.wechat.MessageTextEntity;
import com.payplus.common.wechat.WeChatSignatureUtil;
import com.payplus.common.wechat.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/wechat/portal/")
public class WeChatPortalController {

    @Value("${wechat.config.originalid}")
    private String originalid;
    @Value("${wechat.config.token}")
    private String token;

    @RequestMapping(value = "/receive", method = RequestMethod.GET, produces = "text/plain;charset=utf-8")
    public String validate(@RequestParam(name = "signature") String signature,
                           @RequestParam(name = "timestamp") String timestamp,
                           @RequestParam(name = "nonce") String nonce,
                           @RequestParam(name = "echostr") String echostr) {
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
    public String post(@RequestBody String requestBody) {
        try {
            MessageTextEntity message = XmlUtil.xmlToBean(requestBody, MessageTextEntity.class);
            String openId = message.getFromUserName();
            log.info("WeChat 公众号 request receive {} started {}", openId, requestBody);
            return buildMessageTextEntity(openId, "WeChat 公众平台 receives request, your last message is: " + message.getContent());
        } catch (Exception e) {
            log.error("WeChat 公众号 request receive failed {}", requestBody, e);
            return "";
        }
    }

    private String buildMessageTextEntity(String openId, String content) {
        MessageTextEntity res = new MessageTextEntity();
        // 发送方账号, 公众号分配的Id
        res.setFromUserName(originalid);
        // 开发者微信号
        res.setToUserName(openId);
        // 消息创建时间 （整型）
        res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
        // 消息类型，文本为text
        res.setMsgType("text");
        res.setContent(content);
        return XmlUtil.beanToXml(res);
    }
}
