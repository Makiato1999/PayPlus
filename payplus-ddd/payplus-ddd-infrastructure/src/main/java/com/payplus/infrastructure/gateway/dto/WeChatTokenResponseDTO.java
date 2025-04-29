package com.payplus.infrastructure.gateway.dto;

import lombok.Data;

@Data
public class WeChatTokenResponseDTO {
    private String access_token;
    private Integer expires_in;
    private Integer errcode;
    private String errmsg;
}
