package com.tongkang.wxshop.service;

import com.tongkang.api.DataStatus;
import com.tongkang.wxshop.entity.HttpException;
import com.tongkang.wxshop.entity.PageResponse;
import com.tongkang.wxshop.generator.Shop;
import com.tongkang.wxshop.generator.ShopExample;
import com.tongkang.wxshop.generator.ShopMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class ShopService {
    private ShopMapper shopMapper;

    @Autowired
    public ShopService(ShopMapper shopMapper) {
        this.shopMapper = shopMapper;
    }

    public Shop createShop(Shop shop, Long createId) {
        shop.setOwnerUserId(createId);

        shop.setCreatedAt(new Date());
        shop.setUpdatedAt(new Date());
        shop.setStatus(DataStatus.OK.getName());
        long shopId = shopMapper.insert(shop);
        shop.setId(shopId);
        return shop;
    }

    public Shop deleteShop(Long shopId, Long userId) {
        Shop shopInDatabase = shopMapper.selectByPrimaryKey(shopId);

        if (shopInDatabase == null || shopInDatabase.getStatus().equals("deleted")) {
            throw HttpException.notFound("店铺未找到");
        }

        if (!Objects.equals(shopInDatabase.getOwnerUserId(), userId)) {
            throw HttpException.forbidden("无权访问");
        }

        shopInDatabase.setStatus(DataStatus.DELETED.getName());
        shopInDatabase.setUpdatedAt(new Date());
        shopMapper.updateByPrimaryKey(shopInDatabase);
        return shopInDatabase;
    }

    public Shop updateShop(Shop shop, Long userId) {
        Shop shopInDatabase = shopMapper.selectByPrimaryKey(shop.getId());

        if (shopInDatabase == null || shopInDatabase.getStatus().equals("deleted")) {
            throw HttpException.notFound("店铺未找到");
        }

        if (!Objects.equals(shopInDatabase.getOwnerUserId(), userId)) {
            throw HttpException.forbidden("无权访问");
        }
        shop.setUpdatedAt(new Date());
        shop.setCreatedAt(shopInDatabase.getCreatedAt());
        shop.setOwnerUserId(shopInDatabase.getOwnerUserId());
        shopMapper.updateByPrimaryKey(shop);

        return shop;
    }

    public PageResponse<Shop> getShopByUserId(Long userId, Integer pageNum, Integer pageSize) {
        ShopExample countByStatus = new ShopExample();
        countByStatus.createCriteria().andStatusEqualTo(DataStatus.OK.getName());
        int totalNumber = (int) shopMapper.countByExample(countByStatus);
        int totalPage = totalNumber % pageSize == 0 ? totalNumber / pageSize : totalNumber / pageSize + 1;

        ShopExample pageCondition = new ShopExample();
        pageCondition.createCriteria().andStatusEqualTo(DataStatus.OK.getName());
        pageCondition.setLimit(pageSize);
        pageCondition.setOffset((pageNum - 1) * pageSize);

        List<Shop> pageedShops = shopMapper.selectByExample(pageCondition);

        return PageResponse.pagedData(pageNum, pageSize, totalPage, pageedShops);
    }
}
