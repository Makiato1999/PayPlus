package com.payplus.types.sdk.wechat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.stream.Collectors;

public class WeChatSignatureUtil {
    public static boolean verify(String token, String signature, String timestamp, String nonce) {
        // 1）将token、timestamp、nonce三个参数进行字典序排序
        String[] strArray = {token, timestamp, nonce};
        // 2）将三个参数字符串拼接成一个字符串进行sha1加密
        String sortedStr = Arrays.stream(strArray).sorted().collect(Collectors.joining());
        String encryptedStr = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(sortedStr.getBytes());
            encryptedStr = byteToHexToStr(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // 3）开发者获得加密后的字符串可与signature对比，标识该请求来源于微信
        if (encryptedStr.equals(signature)) {
            return true;
        } else {
            return false;
        }
    }

    private static String byteToHexToStr(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}
