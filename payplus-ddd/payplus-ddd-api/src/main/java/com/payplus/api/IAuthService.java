package com.payplus.api;

import com.payplus.api.response.Response;

public interface IAuthService {
    Response<String> weChatQrCodeTicket();

    Response<String> checkLogin(String ticket);
}
