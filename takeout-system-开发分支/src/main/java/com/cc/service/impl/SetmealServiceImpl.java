package com.cc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.common.CustomerException;
import com.cc.dto.SetmealDto;
import com.cc.mapper.SetmealMapper;
import com.cc.pojo.Setmeal;
import com.cc.pojo.SetmealDish;
import com.cc.service.SetmealDishService;
import com.cc.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;







///**
// * <p>
// * 套餐 服务实现类
// * </p>
// *
// * @author cc
// * @since 2022-05-30
// */
//@Service
//@Slf4j
//public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
//
//    @Autowired
//    private SetmealService setmealService;
//    @Autowired
//    private SetmealDishService setmealDishService;
//
//    /**
//     * 保存套餐信息，商品信息一起就保存了
//     * 保存套餐和商品的关联关系
//     * @param setmealDto
//     */
//    @Override
//    @Transactional
//    public void saveWithDish(SetmealDto setmealDto) {
//        //直接存，因为setmealDto是Setmeal的子类，所以会把SetmealDto中的Setmeal内容存入表中
//        setmealService.save(setmealDto);
//        //下一步就是拿到与套餐关联的商品列表
//        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
//        //商品列表里面还没赋值SetmealId，所以要遍历赋值关联的setmealDish的id
//        for (SetmealDish s : setmealDishes) {
//            s.setSetmealId(setmealDto.getId());
//        }
//        //批量存储套餐信息，插入多条数据
//        setmealDishService.saveBatch(setmealDishes);
//    }
//
//    /**
//     * 获取套餐详细信息，填充到页面上
//     * @param id
//     * @return
//     */
//    @Override
//    public SetmealDto getSetmealData(Long id) {
//        Setmeal setmeal = this.getById(id);
//        SetmealDto setmealDto = new SetmealDto();
//
//        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(id != null,SetmealDish::getSetmealId,id);
//
//        if (setmeal != null){
//            BeanUtils.copyProperties(setmeal,setmealDto);
//
//            List<SetmealDish> dishes = setmealDishService.list(queryWrapper);
//            setmealDto.setSetmealDishes(dishes);
//
//            return setmealDto;
//        }
//
//        return null;
//    }
//
//
//    /**
//     * 修改商品
//     * @param setmealDto
//     */
//    @Transactional
//    public void updateWithDish(SetmealDto setmealDto) {
//        //更新套餐表的套餐
//        this.updateById(setmealDto);
//
//        //查询并删除旧的套餐中的商品
//        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
//        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
//        setmealDishService.remove(queryWrapper);
//
//        //更新传过来的新的商品，并将其赋予套餐的id
//        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
//        setmealDishes=setmealDishes.stream().map((item)->{
//            item.setSetmealId(setmealDto.getId());
//            return item;
//        }).collect(Collectors.toList());
//
//        //将新传过来的套餐中的商品其保存到setmeal_dish表中
//        setmealDishService.saveBatch(setmealDishes);
//    }
//
//
//
//    /**
//     * 删除套餐操作
//     * 删除的时候，套餐下的关联关系也需要删除掉，要同时处理两张表
//     * @param ids 接收多个id，id可以单个也可以多个，批量删或者单个删都可，毕竟走的都是遍历删除
//     */
//    @Override
//    public void removeWithDish(List<Long> ids) {
//        //统计符合条件删除的对象，确定是停售状态才可以删除
//        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper();
//        //利用SQL中的in，来查询这个表内有ids的内容
//        lambdaQueryWrapper.in(Setmeal::getId, ids);
//        lambdaQueryWrapper.eq(Setmeal::getStatus, 1);
//        //统计符合删除条件的套餐个数
//        int count = this.count(lambdaQueryWrapper);
//        //不能删除抛异常
//        if (count>0){
//            throw new CustomerException("不符合删除条件");
//        }
//        //先删除套餐表setmeal内的信息
//        this.removeByIds(ids);
//        //再删除和套餐表相关的信息，直接调用setmealDish的删除方法传id是不行的，因为是Setmeal的id
//        //所以先构造一个查询条件，把setmealDish中id关联setmealId字段的内容查出来
//        LambdaQueryWrapper<SetmealDish> setmealDishlambdaQueryWrapper = new LambdaQueryWrapper<>();
//        setmealDishlambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
//        //条件构造好了，直接remove就行
//        setmealDishService.remove(setmealDishlambdaQueryWrapper);
//    }
//}


@Transactional
@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐，同时需要保存套餐和商品的关联关系
     * @param setmealDto
     */
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //stream流在高数据量下的效率比foreach高，所以使用它
        setmealDishes.stream().map((item) -> {
            //将套餐产品表setmeal_dish数据插入，其中表的套餐id设为SetmealDto数据传入后台后，通过雪花算法随机产生的
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套餐和商品的关联信息，操作setmeal_dish,执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 根据套餐id查询套餐中的商品
     */
    public SetmealDto getByIdWithDish(long id) {
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto=new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);

        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(setmealDishList);

        return setmealDto;
    }

    /**
     * 修改商品
     * @param setmealDto
     */
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {
        //更新套餐表的套餐
        this.updateById(setmealDto);

        //查询并删除旧的套餐中的商品
        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        //更新传过来的新的商品，并将其赋予套餐的id
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes=setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //将新传过来的套餐中的商品其保存到setmeal_dish表中
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 套餐批量删除和单个删除
     */
    @Transactional
    public void deleteWithDish(List<Long> ids) {
        //查询套餐状态，确定是否可用删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);

        //查询选中的并且在启售中的套餐数量
        int count = this.count(queryWrapper);
        if(count > 0){
            //count>0，说明有启售的套餐，全部不允许删除
            //如果不能删除，抛出一个业务异常
            throw new CustomerException("套餐正在售卖中，不能删除");
        }

        //如果可以删除，先删除套餐表中的数据---setmeal
        this.removeByIds(ids);

        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        //删除关系表中的数据----setmeal_dish
        setmealDishService.remove(lambdaQueryWrapper);
    }

}