package com.cc.service;

import com.cc.pojo.ShoppingCart;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 购物车 服务类
 * </p>
 *
 * @author cc
 * @since 2022-05-30
 */
public interface ShoppingCartService extends IService<ShoppingCart> {

    /**
     * 添加商品套餐到购物车
     */
    public ShoppingCart add(ShoppingCart shoppingCart);

    /**
     * 减少商品套餐到购物车
     */
    public ShoppingCart sub(ShoppingCart shoppingCart);

    /**
     * 清空购物车
     */
    public void clean();

}
