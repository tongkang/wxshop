package com.tongkang.wxshop.controller;

import com.tongkang.wxshop.entity.HttpException;
import com.tongkang.wxshop.entity.PageResponse;
import com.tongkang.wxshop.entity.Response;
import com.tongkang.wxshop.generator.Shop;
import com.tongkang.wxshop.service.ShopService;
import com.tongkang.wxshop.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1")
public class ShopController {
    private ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    //创建店铺
    @PostMapping("/shop")
    public Response<Shop> createShop(@RequestBody Shop shop, HttpServletResponse response) {
        Response<Shop> result = Response.of(shopService.createShop(shop, UserContext.getCurrentUser().getId()));
        response.setStatus(HttpStatus.CREATED.value());
        return result;
    }

    //删除店铺
    @DeleteMapping("/shop/{id}")
    public Response<Shop> deleteShop(@PathVariable("id") Long shopId, HttpServletResponse response) {
        try {
            return Response.of(shopService.deleteShop(shopId, UserContext.getCurrentUser().getId()));
        } catch (HttpException e) {
            response.setStatus(e.getStatusCode());
            return Response.of(e.getMessage(), null);
        }
    }

    //修改店铺
    @PatchMapping("/shop/{id}")
    public Response<Shop> updateShop(@PathVariable("id") Long id,
                                     @RequestBody Shop shop,
                                     HttpServletResponse response) {
        try {
            return Response.of(shopService.updateShop(shop, UserContext.getCurrentUser().getId()));
        } catch (HttpException e) {
            response.setStatus(e.getStatusCode());
            return Response.of(e.getMessage(), null);
        }
    }

    //查找所有店铺
    @GetMapping("/shop")
    public PageResponse<Shop> getShop(@RequestParam("pageNum") Integer pageNum,
                                      @RequestParam("pageSize") Integer pageSize) {
        return shopService.getShopByUserId(UserContext.getCurrentUser().getId(), pageNum, pageSize);
    }

}
