package com.tongkang.wxshop.service;

import com.tongkang.wxshop.controller.AuthController;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class TelVerficationService {

    private static Pattern TEL_PATERN = Pattern.compile("1[345678]\\d{9}");

    /**
     * 验证输入参数是否合法
     * tel必须存在且为合法的中国大陆手机号
     *
     * @param param 输入的参数
     * @return true 合法，否则返回false
     */
    public boolean verifyTelParameter(AuthController.TelAndCode param) {
        if (param == null) {
            return false;
        } else if (param.getTel() == null) {
            return false;
        } else {
            return TEL_PATERN.matcher(param.getTel()).find();
        }
    }
}
