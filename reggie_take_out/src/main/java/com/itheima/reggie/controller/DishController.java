package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// 菜品管理
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;
    // 新增菜品
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        dishService.saveWithFlavor(dishDto);
        String key = "dish_" +dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);

        return R.success("新增菜品成功");
    }
//    菜品信息的分页
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        // 构造分页构造器---mybatisplus
        Page<Dish> pageInfo= new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage= new Page<>();
        // 构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        // 添加过滤条件  -- 模糊查询
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);  // name不等于空时才会添加进条件  Employee::getName相当于表的字段名
        // 添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        // 执行查询
        dishService.page(pageInfo, queryWrapper); // 查询结果将会自动填充到 pageInfo 对象中

        // 对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records"); // pageInfo拷贝到dishDtoPage

        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item)->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
            return  dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

//    根据id查询菜品信息和对应的口味信息
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    // 修改菜品
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){

        dishService.updateWithFlavor(dishDto);

        // 清理所有菜品的缓存数据
        // Set keys = redisTemplate.keys("dish_*");
        // redisTemplate.delete(keys);

        // 清理某个分类下的菜品缓存数据
        String key = "dish_" +dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);
        return R.success("修改菜品成功");
    }

//    // 根据条件查询对应的菜品数据
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){   // 前端只传了一个id，dish的id可以接收，其他为null，不用加注解
//        // 构造查询条件
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId()!=null, Dish::getCategoryId, dish.getCategoryId());
//        // 添加条件，查询状态为1(起售状态)的菜品 停售的不需要查出来
//        queryWrapper.eq(Dish::getStatus, 1);
//        // 添加排序条件
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//
//        List<Dish> list = dishService.list(queryWrapper);
//        return R.success(list);
//    }

    // 根据条件查询对应的菜品数据   -- 增加菜品口味的数据
//    @GetMapping("/list")
//    public R<List<DishDto>> list(Dish dish){   // 前端只传了一个CategoryId，dish的id可以接收，其他为null，不用加注解
//        List<DishDto> dishDtoList = null;
//        // 动态构造key
//        String key = "dish_"+dish.getCategoryId()+"_"+dish.getStatus();
//        // 先从redis中获取缓存数据
//        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
//        if(dishDtoList !=null ){
//            // 如果存在，直接返回，无需查询数据库'
//            return R.success(dishDtoList);
//        }
//
//
//        // 构造查询条件
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId()!=null, Dish::getCategoryId, dish.getCategoryId());
//        // 添加条件，查询状态为1(起售状态)的菜品 停售的不需要查出来
//        queryWrapper.eq(Dish::getStatus, 1);
//        // 添加排序条件
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//
//        List<Dish> list = dishService.list(queryWrapper);
//        dishDtoList = list.stream().map((item)->{
//            DishDto dishDto = new DishDto();
//            BeanUtils.copyProperties(item, dishDto);
//            Long categoryId = item.getCategoryId();
//            Category category = categoryService.getById(categoryId);
//            if (category != null){
//                String categoryName = category.getName();
//                dishDto.setCategoryName(categoryName);
//            }
//            // 当前菜品的id
//            Long dishId = item.getId();
//            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//            lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
//            List<DishFlavor> dishFlavors = dishFlavorService.list(lambdaQueryWrapper);
//            dishDto.setFlavors(dishFlavors);
//            return  dishDto;
//        }).collect(Collectors.toList());
//
//        // 如果不存在，需要查询数据库，将查询到的菜品数据缓存到Redis
//        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);  // 缓存一小时
//
//        return R.success(dishDtoList);
//    }

    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){   // 前端只传了一个CategoryId，dish的id可以接收，其他为null，不用加注解
        List<DishDto> dishDtoList = null;
        // 动态构造key
        String key = "dish_"+dish.getCategoryId()+"_"+dish.getStatus();
        // 先从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        if(dishDtoList !=null ){
            // 如果存在，直接返回，无需查询数据库'
            return R.success(dishDtoList);
        }


        // 构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null, Dish::getCategoryId, dish.getCategoryId());
        // 添加条件，查询状态为1(起售状态)的菜品 停售的不需要查出来
        queryWrapper.eq(Dish::getStatus, 1);
        // 添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);


        List<Dish> list = dishService.list(queryWrapper);
        dishDtoList = list.stream().map((item)->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            // 当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> dishFlavors = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavors);
            return  dishDto;
        }).collect(Collectors.toList());

        // 如果不存在，需要查询数据库，将查询到的菜品数据缓存到Redis
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);  // 缓存一小时

        return R.success(dishDtoList);
    }


}
