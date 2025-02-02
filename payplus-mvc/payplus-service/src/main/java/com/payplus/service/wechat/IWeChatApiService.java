package com.payplus.service.wechat;

import com.payplus.domain.req.WeChatQrCodeReq;
import com.payplus.domain.res.WeChatQrCodeRes;
import com.payplus.domain.res.WeChatTokenRes;
import com.payplus.domain.vo.WeChatTemplateMessageVO;

public interface IWeChatApiService {
    /**
     * 获取 Access token
     *
     * @param grantType  获取access_token填写client_credential
     * @param appId      第三方用户唯一凭证
     * @param appSecret  第三方用户唯一凭证密钥，即appsecret
     * @return 响应结果
     */
    WeChatTokenRes getToken(String grantType, String appId, String appSecret);

    /**
     * 获取凭据 ticket (二维码)
     *
     * @param token             getToken 获取的 token 信息
     * @param weChatQrCodeReq   入参对象
     * @return 应答结果
     */
    WeChatQrCodeRes createQrCode(String token, WeChatQrCodeReq weChatQrCodeReq);

    /**
     * 发送微信公众号模板消息
     *
     * @param accessToken                getToken 获取的 token 信息
     * @param weChatTemplateMessageVO    入参对象
     * @return 应答结果
     */
    void sendMessage(String accessToken, WeChatTemplateMessageVO weChatTemplateMessageVO);
}
