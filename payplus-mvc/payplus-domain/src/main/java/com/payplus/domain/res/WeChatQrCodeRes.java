package com.payplus.domain.res;

import lombok.Data;

@Data
public class WeChatQrCodeRes {
    private String ticket;
    private Long expire_seconds;
    private String url;
    private Integer errcode;
    private String errmsg;
}
