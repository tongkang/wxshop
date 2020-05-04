package com.tongkang.wxshop.entity;

import com.tongkang.wxshop.generator.Goods;

public class ShoppingCartGoods extends Goods {
    private int number;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
