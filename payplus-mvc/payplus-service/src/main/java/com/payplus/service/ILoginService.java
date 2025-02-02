package com.payplus.service;

/**
 * @description 微信服务
 */

public interface ILoginService {
    String createQrCodeTicket() throws Exception;

    String checkLogin(String ticket);

    void saveLoginState(String ticket, String openId);
}
