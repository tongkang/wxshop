package com.tongkang.wxshop.controller;

import com.tongkang.wxshop.dao.GoodsDao;
import com.tongkang.wxshop.entity.Response;
import com.tongkang.wxshop.generator.Goods;
import com.tongkang.wxshop.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@RestController
@RequestMapping("/api/v1")
public class GoodsController {

    private final GoodsService goodsService;

    @Autowired
    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    //创建商品
    @PostMapping("/goods")
    public Response<Goods> createdGoods(@RequestBody Goods goods, HttpServletResponse response) {
        //清洗数据，有可能是恶意传入的数据
        clean(goods);
        response.setStatus(HttpServletResponse.SC_CREATED);
        try {
            return Response.of(goodsService.createGoods(goods));
        } catch (GoodsService.NotAuthorizedForShopException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return Response.of(e.getMessage(), null);
        }
    }

    //删除商品
    @DeleteMapping("/goods/{id}")
    public Response<Goods> deleteGoods(@PathVariable("id") Long goodsId, HttpServletResponse response) {
        try {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return Response.of(goodsService.deleteGoodsById(goodsId));
        } catch (GoodsService.NotAuthorizedForShopException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return Response.of(e.getMessage(), null);
        } catch (GoodsDao.ResourceNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return Response.of(e.getMessage(), null);
        }
    }

    private void clean(Goods goods) {
        goods.setId(null);
        goods.setCreatedAt(new Date());
        goods.setUpdatedAt(new Date());
    }
}
