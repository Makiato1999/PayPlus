package com.payplus.domain.auth.service;

import com.google.common.cache.Cache;
import com.payplus.domain.auth.adapter.port.ILoginPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@Service
public class WeChatLoginService implements ILoginService {
    /**
     * 领域只关心业务，遇到需要外部资源，就通过Port接口调用，不自己直接操作外部系统。
     * 自己份内的事直接做，做不了就 call port！
     */
    @Resource
    private ILoginPort loginPort;
    @Resource
    private Cache<String, String> openIdTokenCache;

    @Override
    public String createQrCodeTicket() throws Exception {
        return loginPort.createQrCodeTicket();
    }

    @Override
    public String checkLogin(String ticket) {
        return openIdTokenCache.getIfPresent(ticket);
    }

    @Override
    public void saveLoginState(String ticket, String openId) throws IOException {
        // 保存登陆信息
        openIdTokenCache.put(ticket, openId);

        loginPort.sendLoginTemplateMessage(openId);
    }
}
