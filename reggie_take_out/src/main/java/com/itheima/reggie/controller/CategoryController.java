package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 分类管理
@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    // 新增分类
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        categoryService.save(category);
        return R.success("新增分类成功");
    }
    // 根据id删除分类
    @DeleteMapping
    public R<String> delete(@RequestParam("ids") Long id){   //ids是前端传的id
        log.info("删除分类，id为：{}", id);
        // 要看是否关联到菜品-dish和套餐-setmeal
        categoryService.remove(id);
        return R.success("分类信息删除成功");
    }
    // 修改分类信息

    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改分类信息{}", category);
        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }

    // 分页查询
    @GetMapping("/page")
    public  R<Page> page(int page, int pageSize){
        // 分页构造器
        Page<Category> pageInfo = new Page<>(page, pageSize);
        // 构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper();
        // 添加排序条件
        queryWrapper.orderByDesc(Category::getSort);
        // 执行查询
        categoryService.page(pageInfo, queryWrapper); // 查询结果将会自动填充到 pageInfo 对象中

        return R.success(pageInfo);
    }

    // 根据条件（分类类型）查询分类数据
    @GetMapping("/list")
    public  R<List<Category>> list(Category category){
        // 条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 添加条件   先保证type不为空
        queryWrapper.eq(category.getType() !=null, Category::getType, category.getType());
        // 添加排序条件   如果sort一样，就根据更新时间排序
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }





}
