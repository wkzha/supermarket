package com.cc.service;

import com.cc.pojo.Category;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 商品及套餐分类 服务类
 * </p>
 *
 * @author cc
 * @since 2022-05-30
 */
public interface CategoryService extends IService<Category> {


    void removeCategory(Long id);
}
