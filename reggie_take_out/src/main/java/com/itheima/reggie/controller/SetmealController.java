package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// 套餐管理
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;


    // 新增套餐   涉及两张表，套餐表setmeal、套餐-菜品关系表 setmeal-dish
    @PostMapping
    @CacheEvict(value = "setmealCache" , allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        return R.success(("新增套餐成功"));
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        // 分页构造器对象
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>();
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件，根据name进行like模糊查询
        queryWrapper.like(name!=null, Setmeal::getName, name);
        // 添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo, queryWrapper);

        // 对象拷贝  将 pageInfo 对象中的属性值拷贝到 dtoPage 对象中，除了 "records" 属性
        BeanUtils.copyProperties(pageInfo,dtoPage, "records");
        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();

            BeanUtils.copyProperties(item, setmealDto);
            // 分类id
            Long categoryId = item.getCategoryId();
            // 根据分类id查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                // 分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());
        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    // 删除套餐   只能删除停售的  删除单个和多个是同一个接口，所以传过来的id有多个
    @DeleteMapping
    @CacheEvict(value = "setmealCache" , allEntries = true)   //删除setmealCache下的所有缓存数据
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.removeWithDish(ids);
        return R.success("套餐数据删除成功");
    }

    // 移动端---根据条件查询套餐
    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId+'_'+ #setmeal.status")
    // setmealCache是缓存名称  #setmeal拿到的是穿过来的参数  将方法返回值放入缓存
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null, Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }


}
