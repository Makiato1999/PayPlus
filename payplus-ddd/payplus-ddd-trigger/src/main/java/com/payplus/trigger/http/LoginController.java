package com.payplus.trigger.http;

import com.payplus.api.IAuthService;
import com.payplus.api.response.Response;
import com.payplus.domain.auth.service.ILoginService;
import com.payplus.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/login/")
public class LoginController implements IAuthService {
    @Resource
    private ILoginService loginService;

    /*
    http://makiatox-studio.natapp1.cc/api/v1/login/wechat_qrcode_ticket
     */
    @RequestMapping(value = "wechat_qrcode_ticket", method = RequestMethod.GET)
    public Response<String> weChatQrCodeTicket() {
        try {
            String qrCodeTicket = loginService.createQrCodeTicket();
            log.info("生成微信扫码登陆成功, ticket:{}", qrCodeTicket);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(qrCodeTicket)
                    .build();
        } catch (Exception e) {
            log.error("生成微信扫码登陆ticket失败", e);
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
            String openIdToken = loginService.checkLogin(ticket);
            log.info("扫码检测登陆结果, ticket:{} openIdToken:{}", ticket, openIdToken);
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
            log.error("扫码检测登陆结果失败, ticket:{}", ticket, e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
