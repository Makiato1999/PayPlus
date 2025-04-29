package com.payplus.infrastructure.adapter.port;

import com.google.common.cache.Cache;
import com.payplus.domain.auth.adapter.port.ILoginPort;
import com.payplus.infrastructure.gateway.IWeChatApiService;
import com.payplus.infrastructure.gateway.dto.WeChatQrCodeRequestDTO;
import com.payplus.infrastructure.gateway.dto.WeChatQrCodeResponseDTO;
import com.payplus.infrastructure.gateway.dto.WeChatTemplateMessageDTO;
import com.payplus.infrastructure.gateway.dto.WeChatTokenResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class LoginPort implements ILoginPort {
    @Value("${wechat.config.app-id}")
    private String appId;

    @Value("${wechat.config.app-secret}")
    private String appSecret;

    @Value("${wechat.config.template-id}")
    private String templateId;

    @Resource
    private Cache<String, String> weChatAccessTokenCache;

    @Resource
    private IWeChatApiService weChatApiService;

    @Override
    public String createQrCodeTicket() throws IOException {
        // 获得access token
        String accessToken = weChatAccessTokenCache.getIfPresent(appId);
        if (accessToken == null) {
            Call<WeChatTokenResponseDTO> call = weChatApiService.getToken("client_credential", appId, appSecret);
            WeChatTokenResponseDTO weChatTokenRes = call.execute().body();
            assert  weChatTokenRes != null;
            accessToken = weChatTokenRes.getAccess_token();
            weChatAccessTokenCache.put(appId, accessToken);
        }
        // 以上是存入本地缓存，可以试一下存入redis做分布式缓存

        // 生成ticket
        WeChatQrCodeRequestDTO weChatQrCodeReq = WeChatQrCodeRequestDTO.builder()
                .expire_seconds(2592000)
                .action_name(WeChatQrCodeRequestDTO.ActionNameTypeVO.QR_SCENE.getCode())
                .action_info(WeChatQrCodeRequestDTO.ActionInfo.builder()
                        .scene(WeChatQrCodeRequestDTO.ActionInfo.Scene.builder()
                                .scene_id(100601)
                                .build())
                        .build())
                .build();
        Call<WeChatQrCodeResponseDTO> call = weChatApiService.createQrCode(accessToken, weChatQrCodeReq);
        WeChatQrCodeResponseDTO weChatQrCodeRes = call.execute().body();
        assert weChatQrCodeRes != null;
        return weChatQrCodeRes.getTicket();
    }

    @Override
    public void sendLoginTemplateMessage(String openId) throws IOException {
        // 1. 获取 accessToken 【实际业务场景，按需处理下异常】
        String accessToken = weChatAccessTokenCache.getIfPresent(appId);
        if (null == accessToken) {
            Call<WeChatTokenResponseDTO> call = weChatApiService.getToken("client_credential", appId, appSecret);
            WeChatTokenResponseDTO weChatTokenRes = call.execute().body();
            assert weChatTokenRes != null;
            accessToken = weChatTokenRes.getAccess_token();
            weChatAccessTokenCache.put(appId, accessToken);
        }

        // 2. 发送模板消息
        Map<String, Map<String, String>> data = new HashMap<>();
        WeChatTemplateMessageDTO.put(data, WeChatTemplateMessageDTO.TemplateKey.USER, openId);

        WeChatTemplateMessageDTO templateMessageDTO = new WeChatTemplateMessageDTO(openId, templateId);
        templateMessageDTO.setUrl("https://github.com/Makiato1999");
        templateMessageDTO.setData(data);

        weChatApiService.sendMessage(accessToken, templateMessageDTO);
    }
}
