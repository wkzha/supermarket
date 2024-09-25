package com.cc.service;

import com.cc.dto.SetmealDto;
import com.cc.pojo.Setmeal;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


///**
// * <p>
// * 套餐 服务类
// * </p>
// *
// * @author cc
// * @since 2022-05-30
// */
//public interface SetmealService extends IService<Setmeal> {
//
//    public void saveWithDish(SetmealDto setmealDto);
//
//    public SetmealDto getSetmealData(Long id);
//
//    public void updateWithDish(SetmealDto setmealDto);
//
//
//    public void removeWithDish(List<Long> ids);
//
//}



public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐，同时需要保存套餐和商品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 根据套餐id查询套餐中的商品
     */
    public SetmealDto getByIdWithDish(long id);

    /**
     * 修改商品
     */
    public void updateWithDish(SetmealDto setmealDto);

    /**
     * 套餐批量删除和单个删除
     */
    public void deleteWithDish(List<Long> ids);
}
