package com.tongkang.wxshop.service;

public interface SmsCodeService {

    /**
     * 向指定一个手机号发送验证码，返回正确答案
     * @param tel
     * @return 正确答案
     */
    String sendSmsCode(String tel);
}
