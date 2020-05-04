package com.tongkang.wxshop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tongkang.wxshop.WxshopApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class GoodsIntegrationTest extends AbstractIntegrationTest {


    /**
     * 在创建和删除商品需要重新登录
     * 所以把登录操作抽取出来
     * 让登录测试和商品测试集成父类
     */

    @Test
    public void testCreateGoods() throws JsonProcessingException {

//        UserLoginResponse loginResponse = loginAndGetCookie();
//
//        Goods goods = new Goods();
//        goods.setName("肥皂");
//        goods.setDescription("纯天然无污染肥皂");
//        goods.setDetails("这是一块好肥皂");
//        goods.setImgUrl("http://url");
//        goods.setPrice(1000L);
//        goods.setStock(10);
//
//        HttpResponse response = doHttpRequest(
//                "api/v1/goods",
//                "POST",
//                goods,
//                loginResponse.cookie);
//        Response<Goods> goodsResponse = objectMapper.readValue(response.body, new TypeReference<Response<Goods>>() {
//        });
//
//        assertEquals(SC_CREATED, response.code);
//        assertEquals("肥皂", goods.getName());
    }
}
