package com.tongkang.wxshop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;
import com.tongkang.wxshop.WxshopApplication;
import com.tongkang.wxshop.controller.ShoppingCartController;
import com.tongkang.wxshop.entity.PageResponse;
import com.tongkang.wxshop.entity.Response;
import com.tongkang.wxshop.entity.ShoppingCartData;
import com.tongkang.wxshop.entity.ShoppingCartGoods;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class ShoppingCartIntegrationTest extends AbstractIntegrationTest {
    @Test
    public void canQueryShoppingCartData() throws JsonProcessingException {
        UserLoginResponse loginResponse = loginAndGetCookie();
        PageResponse<ShoppingCartData> response = doHttpRequest("/api/v1/shoppingCart?pageNum=2&pageSize=1",
                "GET", null, loginResponse.cookie).asJsonObject(new TypeReference<PageResponse<ShoppingCartData>>() {
        });

        Assertions.assertEquals(2, response.getPageNum());
        Assertions.assertEquals(1, response.getPageSize());
        Assertions.assertEquals(2, response.getTotalPage());
        Assertions.assertEquals(1, response.getData().size());
        Assertions.assertEquals(2, response.getData().get(0).getShop().getId());
        Assertions.assertEquals(Arrays.asList(4L, 5L),
                response.getData().get(0).getGoods().stream()
                        .map(Goods::getId).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(100L, 200L),
                response.getData().get(0).getGoods().stream()
                        .map(ShoppingCartGoods::getPrice).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(200, 300),
                response.getData().get(0).getGoods().stream()
                        .map(ShoppingCartGoods::getNumber).collect(Collectors.toList()));
    }

    @Test
    public void canAddShoppingCartData() throws Exception {
        UserLoginResponse loginResponse = loginAndGetCookie();

        ShoppingCartController.AddToShoppingCartRequest request = new ShoppingCartController.AddToShoppingCartRequest();
        ShoppingCartController.AddToShoppingCartItem item = new ShoppingCartController.AddToShoppingCartItem();
        item.setId(2L);
        item.setNumber(2);

        request.setGoods(Collections.singletonList(item));

        Response<ShoppingCartData> response = doHttpRequest("/api/v1/shoppingCart",
                "POST", request, loginResponse.cookie).asJsonObject(new TypeReference<Response<ShoppingCartData>>() {
        });

        Assertions.assertEquals(1L, response.getData().getShop().getId());
        Assertions.assertEquals(Arrays.asList(1L, 2L),
                response.getData().getGoods().stream().map(Goods::getId).collect(Collectors.toList()));
        Assertions.assertEquals(Sets.newHashSet(2, 100),
                response.getData().getGoods().stream().map(ShoppingCartGoods::getNumber).collect(Collectors.toSet()));
        Assertions.assertTrue(response.getData().getGoods().stream().allMatch(
                goods -> goods.getShopId() == 1L
        ));
    }
}
