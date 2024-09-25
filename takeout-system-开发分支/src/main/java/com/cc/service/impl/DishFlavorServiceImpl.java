package com.cc.service.impl;

import com.cc.pojo.DishFlavor;
import com.cc.mapper.DishFlavorMapper;
import com.cc.service.DishFlavorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 商品口味关系表 服务实现类
 * </p>
 *
 * @author cc
 * @since 2022-05-30
 */
@Transactional
@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {

}
