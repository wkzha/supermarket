package com.cc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.common.Result;
import com.cc.dto.DishDto;
import com.cc.dto.SetmealDto;
import com.cc.pojo.Category;
import com.cc.pojo.Dish;
import com.cc.pojo.Setmeal;
import com.cc.pojo.SetmealDish;
import com.cc.service.CategoryService;
import com.cc.service.DishService;
import com.cc.service.SetmealDishService;
import com.cc.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishService dishService;

    /**
     * 新增套餐，同时需要保存套餐和商品的关联关系
     * @param setmealDto
     */
    @PostMapping
    public Result<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        return Result.success("新增套餐成功");
    }

    /**
     * 套餐信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name){
        Page<Setmeal> setmealPage=new Page<>(page,pageSize);
        Page<SetmealDto> setmealDtoPage=new Page<>();

        //条件构造器，添加过滤条件，添加排序条件
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        //执行分页查询
        setmealService.page(setmealPage,queryWrapper);

        //对象拷贝
        //页面数据只传了分类id，没显示分类名称，要显示还需以下操作
        //排除records对象，自己写records。records是page对象的属性，存放查询到的数据
        BeanUtils.copyProperties(setmealPage,setmealDtoPage,"records");

        List<Setmeal> setmealList=setmealPage.getRecords();
        List<SetmealDto> setmealDtoList=setmealList.stream().map((item)->{
            SetmealDto setmealDto=new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);

            //根据id查询分类对象
            long categoryId=item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }

            return setmealDto;
        }).collect(Collectors.toList());
        setmealDtoPage.setRecords(setmealDtoList);

        return Result.success(setmealDtoPage);
    }

    /**
     * 根据套餐id查询套餐中的商品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<SetmealDto> get(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return Result.success(setmealDto);
    }

    /**
     * 修改套餐
     */
    @PutMapping
    public Result<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithDish(setmealDto);
        return Result.success("修改套餐成功");
    }

    /**
     * 套餐批量删除和单个删除
     * 1.要先判断要删除的商品是否在售卖，如果在售卖不能删除套餐
     * 2.如果可以删除套餐，则将setmeal_dish表中保存的信息一并删除
     * @return
     */
    @DeleteMapping
    public Result<String> delete(@RequestParam("ids") List<Long> ids){
        setmealService.deleteWithDish(ids);
        return Result.success("删除套餐成功");
    }

    /**
     * 套餐批量启售停售和单个启售停售
     * 请求 URL: http://localhost:8080/setmeal/status/1?ids=1415580119015145474,1579663716218183682
     */
    @PostMapping("/status/{status}")
    public Result<String> status(@PathVariable("status") Integer status,@RequestParam List<Long> ids){
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(ids!=null,Setmeal::getId,ids);
        List<Setmeal> setmealList = setmealService.list(queryWrapper);

        for (Setmeal item:setmealList) {
            if(item!=null){
                item.setStatus(status);
                setmealService.updateById(item);
            }
        }
        return Result.success("售卖状态修改成功");
    }

    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     * 请求 URL: http://localhost:8080/dish/list?categoryId=1397844263642378242&status=1
     */
    @GetMapping("/list")
    public Result<List<Setmeal>> list(Setmeal setmeal){
        //移动端的前端传过来套餐分类id，将其包装成Setmeal对象
        //移动端的前端传过来的数据携带了status=1的数据，封装查询后保证展示的都是启售的套餐
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> setmealList = setmealService.list(queryWrapper);
        return Result.success(setmealList);
    }


    /**
     * 移动端点击套餐图片查看套餐具体内容
     * 这里返回的是dto 对象，因为前端需要copies这个属性
     * 前端主要要展示的信息是:套餐中商品的基本信息，图片，商品描述，以及商品的份数
     * @param SetmealId
     * @return
     */
    @GetMapping("/dish/{id}")
    public Result<List<DishDto>> dish(@PathVariable("id") Long SetmealId){
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,SetmealId);
        //获取套餐里面的所有商品，这个就是SetmealDish表里面的数据
        List<SetmealDish> SetmealDish = setmealDishService.list(queryWrapper);

        List<DishDto> dishDtos = SetmealDish.stream().map((setmealDish) -> {
            DishDto dishDto = new DishDto();
            //通过setmealDish表中的商品id去dish表中查询商品，从而获取商品的各类数据
            Long dishId = setmealDish.getDishId();
            Dish dish = dishService.getById(dishId);
            BeanUtils.copyProperties(dish, dishDto);

            return dishDto;
        }).collect(Collectors.toList());

        return Result.success(dishDtos);
    }
}

///**
// * <p>
// * 套餐 前端控制器
// * </p>
// *
// * @author cc
// * @since 2022-05-30
// */
//@RestController
//@RequestMapping("/setmeal")
//@Slf4j
//public class SetmealController {
//
//    @Autowired
//    private SetmealService setmealService;
//
//    @Autowired
//    private DishService dishService;
//
//    @Autowired
//    private SetmealDishService setmealDishService;
//
//    @Resource
//    private SetmealMapper setmealMapper;
//    /**
//     * 新增套餐
//     * @param setmealDto 用套餐Dto对象接收参数
//     * @return
//     */
//    @PostMapping()
//    public Result<String> saveSetmeal(@RequestBody SetmealDto setmealDto) {
//        log.info(setmealDto.toString());
//        //因为是两张表关联查询，所以MP直接查是不可以的，自己写一个，把两个信息关联起来存储
//        setmealService.saveWithDish(setmealDto);
//        return Result.success("保存成功");
//    }
//
//    /**
//     * 套餐模块的分页查询，因为是多表查询，所以直接MP的分页是不行的
//     * 所以这里自己写的Mapper文件，一个SQL+标签动态SQL解决的
//     * @param page 查第几页
//     * @param pageSize 每页条数
//     * @param name 模糊查询
//     * @return
//     */
//    @GetMapping("/page")
//    public Result<Page> pageList(int page, int pageSize, String name) {
//        Page page1 = new Page<>();
//        //传入是page是从0页开始的，所以要-1
//        List<SetmealDish> resultList=setmealMapper.listSetmeal(page-1, pageSize, name);
//        //将分页对象setRecords，不然的话前端不识别。
//        page1.setRecords(resultList);
//        return Result.success(page1);
//    }
//
//
//    /**
//     * 拿到套餐信息，回填前端页面，为后续套餐更新做准备，调用Service层写
//     * @param id ResultFul风格传入参数，接收套餐id对象，用@PathVariable来接收同名参数
//     * @return 返回套餐对象
//     */
//    @GetMapping("/{id}")
//    public Result<SetmealDto> getSetmal(@PathVariable("id") Long id){
//        log.info("获取套餐Id"+id);
//        SetmealDto setmealDto=setmealService.getSetmealData(id);
//        return Result.success(setmealDto);
//    }
//
//    /**
//     * 修改套餐
//     */
//    @PutMapping
//    public Result<String> update(@RequestBody SetmealDto setmealDto){
//        setmealService.updateWithDish(setmealDto);
//        return Result.success("修改套餐成功");
//    }
//
//
//    /**
//     * 删除套餐操作
//     * 删除的时候，套餐下的关联关系也需要删除掉，要同时处理两张表
//     * @param ids 接收多个id，id可以单个也可以多个，批量删或者单个删都可，毕竟走的都是遍历删除
//     * @return
//     */
//    @DeleteMapping()
//    public Result<String> deleteSetmeal(@RequestParam List<Long> ids){
//        log.info("ids:{}", ids);
//        setmealService.removeWithDish(ids);
//        return Result.success("删除成功");
//    }
//
//
//    /**
//     * 这俩都是更新状态操作，一个启售一个停售
//     * @param ids
//     * @return
//     */
//    @PostMapping("/status/0")
//    public Result<String> startSale(Long ids){
//        Setmeal setmeal=setmealService.getById(ids);
//        setmeal.setStatus(0);
//        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//        lambdaQueryWrapper.eq(Setmeal::getId, ids);
//        setmealService.update(setmeal, lambdaQueryWrapper);
//        return Result.success("更新状态为启售");
//    }
//    @PostMapping("/status/1")
//    public Result<String> stopSale(Long ids){
//        Setmeal setmeal=setmealService.getById(ids);
//        setmeal.setStatus(1);
//        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//        lambdaQueryWrapper.eq(Setmeal::getId, ids);
//        setmealService.update(setmeal, lambdaQueryWrapper);
//        return Result.success("更新状态为停售");
//    }
//
//    /**
//     * 消费者前台页面显示套餐相关的内容
//     * 这里不能用RequestBody注解接收参数，是因为传来的参数不是完整的对象并且不是Json，只是对象的一部分
//     * 用k-v形式进行传输，所以不能用RequestBody接收
//     * @param setmeal
//     * @return
//     */
//    @GetMapping("/list")  // 在消费者端 展示套餐信息
//    public Result<List<Setmeal>> list(Setmeal setmeal){
//        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
//        Long categoryId = setmeal.getCategoryId();
//        Integer status = setmeal.getStatus();
//        //种类不为空才查
//        queryWrapper.eq(categoryId != null,Setmeal::getCategoryId,categoryId);
//        //在售状态才查
//        queryWrapper.eq(status != null,Setmeal::getStatus,status);
//
//        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
//
//        List<Setmeal> setmeals = setmealService.list(queryWrapper);
//
//        return Result.success(setmeals);
//    }
//
//    /**
//     * 移动端点击套餐图片查看套餐具体内容
//     * 这里返回的是dto 对象，因为前端需要copies这个属性
//     * 前端主要要展示的信息是:套餐中商品的基本信息，图片，商品描述，以及商品的份数
//     * @param SetmealId
//     * @return
//     */
//    @GetMapping("/dish/{id}")
//    public Result<List<DishDto>> dish(@PathVariable("id") Long SetmealId){
//        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(SetmealDish::getSetmealId,SetmealId);
//        //获取套餐里面的所有商品，这个就是SetmealDish表里面的数据
//        List<SetmealDish> SetmealDish = setmealDishService.list(queryWrapper);
//
//        List<DishDto> dishDtos = SetmealDish.stream().map((setmealDish) -> {
//            DishDto dishDto = new DishDto();
//            //通过setmealDish表中的商品id去dish表中查询商品，从而获取商品的各类数据
//            Long dishId = setmealDish.getDishId();
//            Dish dish = dishService.getById(dishId);
//            BeanUtils.copyProperties(dish, dishDto);
//
//            return dishDto;
//        }).collect(Collectors.toList());
//
//        return Result.success(dishDtos);
//    }
//
//
//}
