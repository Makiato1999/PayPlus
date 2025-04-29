package com.payplus.domain.auth.adapter.port;

import java.io.IOException;

/**
 * 供domain自己用，去要外部资源
 * Port是自己领域里定义好的"中转接口"，真正去用外部资源的是Infrastructure层的Adapter。
 */
public interface ILoginPort {
    String createQrCodeTicket() throws IOException;

    void sendLoginTemplateMessage(String openId) throws IOException;
}
