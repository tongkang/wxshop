package com.tongkang.wxshop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kevinsawicki.http.HttpRequest;
import com.tongkang.wxshop.WxshopApplication;
import com.tongkang.wxshop.entity.LoginResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;

import static com.tongkang.wxshop.service.TelVerficationServiceTest.INVALID_PARAMETER;
import static com.tongkang.wxshop.service.TelVerficationServiceTest.VALID_PARAMETER;
import static com.tongkang.wxshop.service.TelVerficationServiceTest.VALID_PARAMETER_CODE;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
public class AuthIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    Environment environment;

    private static class HttpResponse {
        int code;
        String body;
        Map<String, List<String>> headers;

        HttpResponse(int code, String body, Map<String, List<String>> headers) {
            this.code = code;
            this.body = body;
            this.headers = headers;
        }
    }

//    statusResponse = HttpRequest.get(getUrl("/api/status"))
//            .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .header("Cookie", sessionId)
//                .accept(MediaType.APPLICATION_JSON_VALUE)
//                .body();

//    esponseHeaders = HttpRequest.post(getUrl("/api/logout"))
//            .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .header("Cookie", sessionId)
//                .accept(MediaType.APPLICATION_JSON_VALUE)
//                .headers();
//    int responseCode = HttpRequest.post(getUrl("/api/code"))
//            .contentType(MediaType.APPLICATION_JSON_VALUE)
//            .accept(MediaType.APPLICATION_JSON_VALUE)
//            .send(objectMapper.writeValueAsString(VALID_PARAMETER))
//            .code();

    //重构上面的代码
    private HttpResponse doHttpRequest(String apiName, boolean isGet, Object requestBody, String cookie) throws JsonProcessingException {
        HttpRequest request = isGet ? HttpRequest.get(getUrl(apiName)) : HttpRequest.post(getUrl(apiName));

        if (cookie != null) {
            request.header("Cookie", cookie);
        }
        request.contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE);

        if (requestBody != null) {
            request.send(objectMapper.writeValueAsString(requestBody));
        }
        return new HttpResponse(request.code(), request.body(), request.headers());

    }


    @Test
    public void loginLogoutTest() throws JsonProcessingException {
        //最开始默认情况下，访问/api/status 处于未登录状态
        //发送验证码
        //带着验证码进行登录，得到Cookie
        //带着Cookie 访问，/api/login 应该处于登录状态
        //调用 /api/logou
        //再次带着Cookie访问/api/status 恢复成为未登录状态


        //get请求就不需要带上body内容
        //1.最开始默认情况下，访问/api/status 处于未登录状态
        String statusResponse = doHttpRequest("/api/v1/status", true, null, null).body;

        LoginResponse response = objectMapper.readValue(statusResponse, LoginResponse.class);

        Assertions.assertFalse(response.isLogin());

        //2.发送验证码
        int responseCode = doHttpRequest("/api/v1/code", false, VALID_PARAMETER, null).code;

        Assertions.assertEquals(HTTP_OK, responseCode);

        //3.带着验证码进行登录，得到Cookie
        Map<String, List<String>> responseHeaders = doHttpRequest("/api/v1/login", false, VALID_PARAMETER_CODE, null).headers;

        List<String> setCookie = responseHeaders.get("Set-Cookie");
        String sessionId = getSessionIdFromSetCookie(setCookie.stream().filter(cookie -> cookie.contains("JSESSIONID"))
                .findFirst()
                .get());

        //4.带着Cookie 访问，/api/login 应该处于登录状态
        statusResponse = doHttpRequest("/api/v1/status", true, null, sessionId).body;

        response = objectMapper.readValue(statusResponse, LoginResponse.class);
        Assertions.assertTrue(response.isLogin());
        Assertions.assertEquals(VALID_PARAMETER.getTel(), response.getUser().getTel());

        //5.调用 /api/logou
        //注意，注销登陆也要带着Cookie
        responseHeaders = doHttpRequest("/api/v1/logout", false, null, sessionId).headers;

        //6.再次带着Cookie访问/api/status 恢复成为未登录状态
        statusResponse = doHttpRequest("/api/v1/status", true, null, sessionId).body;

        response = objectMapper.readValue(statusResponse, LoginResponse.class);
        Assertions.assertFalse(response.isLogin());

    }

    private String getSessionIdFromSetCookie(String setCookie) {
        //JSESSIONID=103d9e8a-f4cb-40ce-b22e-d665a0359a4b; Path=/; HttpOnly; SameSite=lax
        int semicolonIndex = setCookie.indexOf(";");
        return setCookie.substring(0, semicolonIndex);
    }

    @Test
    public void returnHttpOkWhenParameterIsCorrect() throws JsonProcessingException {
        int responseCode = doHttpRequest("/api/v1/code", false, VALID_PARAMETER, null).code;

        Assertions.assertEquals(HTTP_OK, responseCode);
    }

    @Test
    public void returnHttpBadRequestWhenParameterIsCorrect() throws JsonProcessingException {
        int responseCode = doHttpRequest("/api/v1/code", false, INVALID_PARAMETER, null).code;

        Assertions.assertEquals(HTTP_BAD_REQUEST, responseCode);

    }

    private String getUrl(String apiName) {
        // 获取集成测试的端口号
        return "http://localhost:" + environment.getProperty("local.server.port") + apiName;
    }

}
