package com.payplus.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import com.google.common.eventbus.EventBus;
import com.payplus.listener.OrderPaySuccessListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class GuavaConfig {

    @Bean(name = "weChatAccessTokenCache")
    public Cache<String, String> weChatAccessToken() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(2, TimeUnit.HOURS)
                .build();
    }

    @Bean(name = "openIdTokenCache")
    public Cache<String, String> openIdToken() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    @Bean(name = "eventBus")
    public EventBus eventBusListener(OrderPaySuccessListener orderPaySuccessListener) {
        EventBus eventBus = new EventBus();
        eventBus.register(orderPaySuccessListener);
        return eventBus;
    }
}
