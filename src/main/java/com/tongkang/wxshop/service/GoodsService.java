package com.tongkang.wxshop.service;

import com.tongkang.wxshop.entity.DataStatus;
import com.tongkang.wxshop.entity.HttpException;
import com.tongkang.wxshop.entity.PageResponse;
import com.tongkang.wxshop.generator.Goods;
import com.tongkang.wxshop.generator.GoodsExample;
import com.tongkang.wxshop.generator.GoodsMapper;
import com.tongkang.wxshop.generator.Shop;
import com.tongkang.wxshop.generator.ShopMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class GoodsService {

    private GoodsMapper goodsMapper;
    private ShopMapper shopMapper;

    public GoodsService(GoodsMapper goodsMapper, ShopMapper shopMapper) {
        this.goodsMapper = goodsMapper;
        this.shopMapper = shopMapper;
    }


    public Goods createGoods(Goods goods) {


//        要在所在的商店中才能进行创建商品
        Shop shop = shopMapper.selectByPrimaryKey(goods.getShopId());

        if (Objects.equals(shop.getOwnerUserId(), UserContext.getCurrentUser().getId())) {
        goods.setStatus(DataStatus.OK.getName());
        long id = goodsMapper.insert(goods);
        goods.setId(id);
        return goods;
        } else {
            throw HttpException.forbidden("无权访问！");
        }
    }

    public Goods deleteGoodsById(Long goodsId) {
        Shop shop = shopMapper.selectByPrimaryKey(goodsId);

        if (Objects.equals(shop.getOwnerUserId(), UserContext.getCurrentUser().getId())) {
            Goods goods = goodsMapper.selectByPrimaryKey(goodsId);
            if (goods == null) {
                throw HttpException.notFound("未找到！");
            }
            goods.setStatus(DataStatus.DELETED.getName());
            goodsMapper.updateByPrimaryKey(goods);

            return goods;
        } else {
            throw HttpException.forbidden("无权访问！");
        }

    }

    public PageResponse<Goods> getGoods(Integer pageNum, Integer pageSize, Integer shopId) {
        //知道有多少个元素
        //然后才知道有多少页
        //然后才能正确的分页
        int totalNumber = countGoods(shopId);
        int totalPage = totalNumber % pageSize == 0 ? totalNumber / pageSize : totalNumber / pageSize + 1;

        GoodsExample page = new GoodsExample();
        page.setLimit(pageSize);
        page.setOffset((pageNum - 1) * pageSize);

        List<Goods> pagedGoods = goodsMapper.selectByExample(page);

        return PageResponse.pagedData(pageNum, pageSize, totalPage, pagedGoods);
    }

    private int countGoods(Integer shopId) {
        if (shopId == null) {
            GoodsExample goodsExample = new GoodsExample();
            goodsExample.createCriteria()
                    .andStatusEqualTo(DataStatus.OK.getName());
            return (int) goodsMapper.countByExample(goodsExample);
        } else {
            GoodsExample goodsExample = new GoodsExample();
            goodsExample.createCriteria()
                    .andStatusEqualTo(DataStatus.OK.getName())
                    .andShopIdEqualTo(shopId.longValue());
            return (int) goodsMapper.countByExample(goodsExample);
        }
    }

    public Goods updateGoods(Goods goods) {
        Shop shop = shopMapper.selectByPrimaryKey(goods.getShopId());

        if (Objects.equals(shop.getOwnerUserId(), UserContext.getCurrentUser().getId())) {
            GoodsExample byId = new GoodsExample();
            byId.createCriteria().andIdEqualTo(goods.getId());
            int affecteRows = goodsMapper.updateByExample(goods, byId);
            if (affecteRows == 0) {
                throw HttpException.notFound("没找到");
            }
            return goods;
        } else {
            throw HttpException.forbidden("无权访问！");
        }
    }

    public static class NotAuthorizedForShopException extends RuntimeException {
        public NotAuthorizedForShopException(String message) {
            super(message);
        }
    }
}
