package com.cc.service.impl;

import com.cc.pojo.SetmealDish;
import com.cc.mapper.SetmealDishMapper;
import com.cc.service.SetmealDishService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;


/**
 * <p>
 * 套餐商品关系 服务实现类
 * </p>
 *
 * @author cc
 * @since 2022-05-30
 */
@Transactional
@Slf4j
@Service
public class SetmealDishServiceImpl extends ServiceImpl<SetmealDishMapper, SetmealDish> implements SetmealDishService {

}
