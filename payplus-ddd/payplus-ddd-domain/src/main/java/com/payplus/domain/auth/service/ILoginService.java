package com.payplus.domain.auth.service;

import java.io.IOException;

/**
 * 供 Controller 调用，提供业务能力
 */

public interface ILoginService {
    String createQrCodeTicket() throws Exception;

    String checkLogin(String ticket);

    void saveLoginState(String ticket, String openId) throws IOException;
}
