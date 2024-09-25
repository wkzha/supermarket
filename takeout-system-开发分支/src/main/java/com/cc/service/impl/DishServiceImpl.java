package com.cc.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.common.CustomerException;
import com.cc.dto.DishDto;
import com.cc.mapper.DishMapper;
import com.cc.mapper.DishRepository;
import com.cc.pojo.Dish;
import com.cc.pojo.DishFlavor;
import com.cc.pojo.Setmeal;
import com.cc.pojo.SetmealDish;
import com.cc.service.DishFlavorService;
import com.cc.service.DishService;
import com.cc.service.SetmealDishService;
import com.cc.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 商品管理 服务实现类
 * </p>
 *
 * @author cc
 * @since 2022-05-30
 */
@Transactional
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private DishService dishService;

    /**
     * 多表操作只能一个一个来，MP没有办法一次性操作多张表
     * 因为涉及到多表的问题，所以还要加入注解来处理事务
     *
     * @param dishDto
     * @Transactional 开启事务
     * @EnableTransactionManagement 在启动类加入，支持事务开启
     */
    @Transactional
    @Override
    public void addDishWithFlavor(DishDto dishDto) {
        //因为DishDto是包含了Dish的信息，所以可以先存Dish信息到Dish表中，DishDto扩展的数据可以下一步再存
        //为什么这里传dishDto可以，因为DishDto是Dish的子类
        dishService.save(dishDto);
        //拿ID和口味List，为存DishDto做准备
        Long dishId = dishDto.getId();
        List<DishFlavor> flavor = dishDto.getFlavors();
        //遍历
        for (DishFlavor dishFlavors : flavor) {
            dishFlavors.setDishId(dishId);
        }
        //saveBatch是批量集合的存储
        dishFlavorService.saveBatch(flavor);
    }

    /**
     * 更新口味操作，和上面的添加操作异曲同工
     *
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateDishWithFlavor(DishDto dishDto) {
        //Dish表是可以直接更新操作的,这里也是一样的，传入的是Dish的子类，可以直接操作，默认也就是按Dish类更新了
        dishService.updateById(dishDto);
        //Dish_Flavor表比较特殊，所以需要先删除再插入
        //Dish_Flavor表字段删除，所有当前dish id的口味
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper();
        //子类可以直接获取父类的内容了
        lambdaQueryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(lambdaQueryWrapper);
        //再插入
        List<DishFlavor> flavorList = dishDto.getFlavors();
        //遍历
        for (DishFlavor dishFlavors : flavorList) {
            dishFlavors.setDishId(dishDto.getId());
        }
        //saveBatch是批量集合的存储
        dishFlavorService.saveBatch(flavorList);
    }


    /**
     * 通过id查询口味信息
     *
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //先把普通信息查出来
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        //搬运
        BeanUtils.copyProperties(dish, dishDto);
        //在通过dish的分类信息查口味List
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> listFlavor = dishFlavorService.list(lambdaQueryWrapper);
        //填充DishDto
        dishDto.setFlavors(listFlavor);
        return dishDto;


    }

    @Transactional
    public void deleteByIds(List<Long> ids) {
        //构造条件查询器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //先查询该商品是否在售卖，如果是则抛出业务异常
        queryWrapper.in(ids != null, Dish::getId, ids);
        List<Dish> list = this.list(queryWrapper);
        for (Dish dish : list) {
            Integer status = dish.getStatus();
            //如果不是在售卖,则可以删除
            if (status == 0) {
                this.removeById(dish.getId());
            } else {
                //此时应该回滚,因为可能前面的删除了，但是后面的是正在售卖
                throw new CustomerException("删除商品中有正在售卖商品,无法全部删除");
            }
        }
    }

    @Transactional
    public boolean deleteInSetmeal(List<Long> ids) {
        boolean flag = true;

        //1.根据商品id在stemeal_dish表中查出哪些套餐包含该商品
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getDishId, ids);
        List<SetmealDish> SetmealDishList = setmealDishService.list(setmealDishLambdaQueryWrapper);
        //2.如果商品没有关联套餐，直接删除就行  其实下面这个逻辑可以抽离出来，这里我就不抽离了
        if (SetmealDishList.size() == 0) {
            //这个deleteByIds中已经做了商品启售不能删除的判断力
            this.deleteByIds(ids);
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(DishFlavor::getDishId, ids);
            dishFlavorService.remove(queryWrapper);
            return flag;
        }

        //3.如果商品有关联套餐，并且该套餐正在售卖，那么不能删除
        //3.1得到与删除商品关联的套餐id
        ArrayList<Long> Setmeal_idList = new ArrayList<>();
        for (SetmealDish setmealDish : SetmealDishList) {
            Long setmealId = setmealDish.getSetmealId();
            Setmeal_idList.add(setmealId);
        }
        //3.2查询出与删除商品相关联的套餐
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId, Setmeal_idList);
        List<Setmeal> setmealList = setmealService.list(setmealLambdaQueryWrapper);
        //3.3对拿到的所有套餐进行遍历，然后拿到套餐的售卖状态，如果有套餐正在售卖那么删除失败
        for (Setmeal setmeal : setmealList) {
            Integer status = setmeal.getStatus();
            if (status == 1) {
                flag = false;
            }
        }

        //3.4要删除的商品关联的套餐没有在售，可以删除
        //3.5这下面的代码并不一定会执行,因为如果前面的for循环中出现status == 1,那么下面的代码就不会再执行
        this.deleteByIds(ids);
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(queryWrapper);

        return flag;
    }

    @Autowired
    private DishRepository dishRepository;

    @Override
    public List<Dish> getDishes() {
        return dishRepository.findAll();
    }

    @Override
    public void updateQuantity(Long id, Integer quantity) {
        Dish dish = dishRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid dish id"));
        dish.setQuantity(quantity);
        dishRepository.save(dish);
    }

    @Override
    public void checkNumberAlert() {
        List<Dish> dishes = dishRepository.findAll();
        for (Dish dish : dishes) {
            if (dish.getQuantity() < dish.getAlertQuantity()) {
                // send alert message to management
                System.out.println("Alert: good " + dish.getName() + " is running low on stock!");
            }
        }

    }

    @Scheduled(fixedRate = 60000) // check every minute
    public void checkNumberAlertTask() {
        checkNumberAlert();
    }

}
