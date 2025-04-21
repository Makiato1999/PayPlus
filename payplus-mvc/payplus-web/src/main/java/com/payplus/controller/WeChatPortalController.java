package com.payplus.controller;

import com.payplus.common.wechat.MessageTextEntity;
import com.payplus.common.wechat.WeChatSignatureUtil;
import com.payplus.common.wechat.XmlUtil;
import com.payplus.service.ILoginService;
import com.payplus.service.impl.WeChatLoginServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/wechat/portal/")
public class WeChatPortalController {

    @Value("${wechat.config.originalId}")
    private String originalId;
    @Value("${wechat.config.token}")
    private String token;

    @Resource
    private WeChatLoginServiceImpl weChatLoginService;

    @RequestMapping(value = "/receive", method = RequestMethod.GET, produces = "text/plain;charset=utf-8")
    public String validate(@RequestParam(name = "signature") String signature,
                           @RequestParam(name = "timestamp") String timestamp,
                           @RequestParam(name = "nonce") String nonce,
                           @RequestParam(name = "echostr") String echostr) {
        try {
            log.info("WeChat 公众号验签信息开始 [{}, {}, {}, {}]",
                    signature, timestamp, nonce, echostr);

            if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
                throw new IllegalArgumentException("请求参数非法，请核实!");
            }

            boolean verifyStatus = WeChatSignatureUtil.verify(token, signature, timestamp, nonce);
            log.info("WeChat 公众号验签信息完成 check：{}", verifyStatus);
            if (verifyStatus) {
                return echostr;
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("WeChat 公众号验签信息失败 [{}, {}, {}, {}]",
                    signature, timestamp, nonce, echostr);
            return null;
        }
    }

    // 当用户在微信中给你的公众号发送消息时，微信服务器会把这个消息打包成 XML 格式，然后以 POST 请求的方式发送到你配置的 URL。
    // 你的 post 方法接收到这个 POST 请求，它会
    //      解析请求体中的 XML，将其转换成一个 MessageTextEntity 对象（里面包含发送者、消息内容等信息）
    //      记录日志，知道哪个用户（openId）发送了消息，以及消息内容是什么
    //      根据接收到的消息构造一个回复消息，告知用户“你上一次发的消息是：XXX”
    //      最后把这个回复消息转换成 XML 格式，再返回给微信服务器
    // 微信服务器收到你的回复 XML 后，再把它转发给用户，用户就能在微信中看到回复内容。
    @RequestMapping(value = "/receive", method = RequestMethod.POST, produces = "application/xml; charset=UTF-8")
    public String post(@RequestBody String requestBody) {
        try {
            MessageTextEntity message = XmlUtil.xmlToBean(requestBody, MessageTextEntity.class);
            String openId = message.getFromUserName();
            log.info("WeChat 公众号信息请求 {} 请求体 {}", openId, requestBody);

            if ("event".equals(message.getMsgType()) && "SCAN".equals(message.getEvent())) {
                weChatLoginService.saveLoginState(message.getTicket(), openId);
                return buildMessageTextEntity(openId, "登陆成功");
            }
            return buildMessageTextEntity(openId, "WeChat 公众平台接收了你的请求, 你的上一条消息是: " + message.getContent());
        } catch (Exception e) {
            log.error("WeChat 公众号请求接收失败 {}", requestBody, e);
            return "";
        }
    }

    private String buildMessageTextEntity(String openId, String content) {
        MessageTextEntity res = new MessageTextEntity();
        // 发送方账号, 公众号分配的Id
        res.setFromUserName(originalId);
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
