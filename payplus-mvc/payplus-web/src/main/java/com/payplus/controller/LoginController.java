package com.payplus.controller;

import com.payplus.common.constants.Constants;
import com.payplus.common.response.Response;
import com.payplus.service.impl.WeChatLoginServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/login/")
public class LoginController {
    @Resource
    private WeChatLoginServiceImpl weChatLoginService;

    /*
    http://makiatox-studio.natapp1.cc/api/v1/login/wechat_qrcode_ticket
     */
    @RequestMapping(value = "wechat_qrcode_ticket", method = RequestMethod.GET)
    public Response<String> weChatQrCodeTicket() {
        try {
            String qrCodeTicket = weChatLoginService.createQrCodeTicket();
            log.info("Generate WeChat QrCode Login successfully, ticket:{}", qrCodeTicket);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(qrCodeTicket)
                    .build();
        } catch (Exception e) {
            log.error("Generate WeChat QrCode Login failed, ticket:", e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /*
    http://makiatox-studio.natapp1.cc/api/v1/login/check_login
     */
    @RequestMapping(value = "check_login", method = RequestMethod.GET)
    public Response<String> checkLogin(@RequestParam String ticket) {
        try {
            String openIdToken = weChatLoginService.checkLogin(ticket);
            log.info("Scan and check Login result successfully, ticket:{} openIdToken:{}", ticket, openIdToken);
            if (StringUtils.isNotBlank(openIdToken)) {
                return Response.<String>builder()
                        .code(Constants.ResponseCode.SUCCESS.getCode())
                        .info(Constants.ResponseCode.SUCCESS.getInfo())
                        .data(openIdToken)
                        .build();
            } else {
                return Response.<String>builder()
                        .code(Constants.ResponseCode.NO_LOGIN.getCode())
                        .info(Constants.ResponseCode.NO_LOGIN.getInfo())
                        .data(openIdToken)
                        .build();
            }
        } catch (Exception e) {
            log.error("Scan and check Login result failed, ticket:{}", ticket, e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
