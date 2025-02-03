package com.payplus.service.impl;

import com.google.common.cache.Cache;
import com.payplus.domain.req.WeChatQrCodeReq;
import com.payplus.domain.res.WeChatQrCodeRes;
import com.payplus.domain.res.WeChatTokenRes;
import com.payplus.domain.vo.WeChatTemplateMessageVO;
import com.payplus.service.ILoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeChatLoginServiceImpl  implements ILoginService {
    @Value("${wechat.config.app-id}")
    private String appId;

    @Value("${wechat.config.app-secret}")
    private String appSecret;

    @Value("${wechat.config.template-id}")
    private String templateId;

    @Resource
    private Cache<String, String> weChatAccessTokenCache;

    @Resource
    private Cache<String, String> openIdTokenCache;

    @Autowired
    private WeChatApiServiceImpl weChatApiService;

    @Override
    public String createQrCodeTicket() throws Exception {
        // 获得access token
        String accessToken = weChatAccessTokenCache.getIfPresent(appId);
        if (accessToken == null) {
            WeChatTokenRes weChatTokenRes = weChatApiService.getToken("client_credential", appId, appSecret);
            assert weChatTokenRes != null;
            accessToken = weChatTokenRes.getAccess_token();
            weChatAccessTokenCache.put(appId, accessToken);
        }
        // 以上是存入本地缓存，可以试一下存入redis做分布式缓存

        // 生成ticket
        WeChatQrCodeReq weChatQrCodeReq = WeChatQrCodeReq.builder()
                .expire_seconds(2592000)
                .action_name(WeChatQrCodeReq.ActionNameTypeVO.QR_SCENE.getCode())
                .action_info(WeChatQrCodeReq.ActionInfo.builder()
                        .scene(WeChatQrCodeReq.ActionInfo.Scene.builder()
                                .scene_id(100601)
                                .build())
                        .build())
                .build();
        WeChatQrCodeRes weChatQrCodeRes = weChatApiService.createQrCode(accessToken, weChatQrCodeReq);
        assert weChatQrCodeRes != null;
        return weChatQrCodeRes.getTicket();
    }

    @Override
    public String checkLogin(String ticket) {
        return openIdTokenCache.getIfPresent(ticket);
    }

    @Override
    public void saveLoginState(String ticket, String openId) {
        openIdTokenCache.put(ticket, openId);

        // 1. 获取 accessToken 【实际业务场景，按需处理下异常】
        String accessToken = weChatAccessTokenCache.getIfPresent(appId);
        if (null == accessToken) {
            WeChatTokenRes weChatTokenRes = weChatApiService.getToken("client_credential", appId, appSecret);
            assert weChatTokenRes != null;
            accessToken = weChatTokenRes.getAccess_token();
            weChatAccessTokenCache.put(appId, accessToken);
        }

        // 2. 发送模板消息
        Map<String, Map<String, String>> data = new HashMap<>();
        WeChatTemplateMessageVO.put(data, WeChatTemplateMessageVO.TemplateKey.USER, openId);

        WeChatTemplateMessageVO templateMessageDTO = new WeChatTemplateMessageVO(openId, templateId);
        templateMessageDTO.setUrl("https://github.com/Makiato1999");
        templateMessageDTO.setData(data);

        weChatApiService.sendMessage(accessToken, templateMessageDTO);
    }
}
