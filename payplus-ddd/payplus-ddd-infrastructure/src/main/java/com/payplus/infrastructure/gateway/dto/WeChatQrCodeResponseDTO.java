package com.payplus.infrastructure.gateway.dto;

import lombok.Data;

@Data
public class WeChatQrCodeResponseDTO {

    private String ticket;
    private Long expire_seconds;
    private String url;

}

