package com.tongkang.wxshop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tongkang.wxshop.WxshopApplication;
import com.tongkang.wxshop.api.OrderService;
import com.tongkang.wxshop.entity.LoginResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.tongkang.wxshop.service.TelVerficationServiceTest.INVALID_PARAMETER;
import static com.tongkang.wxshop.service.TelVerficationServiceTest.VALID_PARAMETER;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    OrderService orderService;

    @Test
    public void loginLogoutTest() throws JsonProcessingException {
        //1、最开始默认情况下，访问/api/status 处于未登录状态
        //2、发送验证码
        //3、带着验证码进行登录，得到Cookie
        //4、带着Cookie 访问，/api/login 应该处于登录状态
        //5、调用 /api/logou
        //6、再次带着Cookie访问/api/status 恢复成为未登录状态



        String sessionId = loginAndGetCookie().cookie;

        //4.带着Cookie 访问，/api/login 应该处于登录状态
        String statusResponse = doHttpRequest("/api/v1/status", "GET", null, sessionId).body;
        LoginResponse response = objectMapper.readValue(statusResponse, LoginResponse.class);
        Assertions.assertTrue(response.isLogin());
        Assertions.assertEquals(VALID_PARAMETER.getTel(), response.getUser().getTel());

        //5.调用 /api/logou
        //注意，注销登陆也要带着Cookie
        doHttpRequest("/api/v1/logout", "POST", null, sessionId);

        //6.再次带着Cookie访问/api/status 恢复成为未登录状态
        statusResponse = doHttpRequest("/api/v1/status", "GET", null, sessionId).body;

        response = objectMapper.readValue(statusResponse, LoginResponse.class);
        Assertions.assertFalse(response.isLogin());

    }


    @Test
    public void returnHttpOkWhenParameterIsCorrect() throws JsonProcessingException {
        int responseCode = doHttpRequest("/api/v1/code", "POST", VALID_PARAMETER, null).code;

        Assertions.assertEquals(HTTP_OK, responseCode);
    }

    @Test
    public void returnHttpBadRequestWhenParameterIsCorrect() throws JsonProcessingException {
        int responseCode = doHttpRequest("/api/v1/code", "POST", INVALID_PARAMETER, null).code;

        Assertions.assertEquals(HTTP_BAD_REQUEST, responseCode);

    }


}
