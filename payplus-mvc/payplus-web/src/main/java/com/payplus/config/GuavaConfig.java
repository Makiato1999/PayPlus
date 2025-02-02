package com.payplus.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class GuavaConfig {

    @Bean("weChatAccessToken")
    public Cache<String, String> weChatAccessToken() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(2, TimeUnit.HOURS)
                .build();
    }

    @Bean("openIdToken")
    public Cache<String, String> openIdToken() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }
}
