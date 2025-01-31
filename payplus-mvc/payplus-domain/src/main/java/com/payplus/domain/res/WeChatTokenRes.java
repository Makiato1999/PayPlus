package com.payplus.domain.res;

import lombok.Data;

@Data
public class WeChatTokenRes {
    private String access_token;
    private Integer expires_in;
    private Integer errcode;
    private String errmsg;
}
