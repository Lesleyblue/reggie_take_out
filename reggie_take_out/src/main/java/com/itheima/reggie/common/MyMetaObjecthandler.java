package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

//自定义元数据对象处理器

@Slf4j
@Component
public class MyMetaObjecthandler implements MetaObjectHandler {  // 插入操作，自动填充
    @Override
    public void insertFill(MetaObject metaObject) {
    metaObject.setValue("createTime", LocalDateTime.now());
    metaObject.setValue("updateTime", LocalDateTime.now());
    metaObject.setValue("createUser", BaseContext.getCurrentId());  // 这里先写死，后面再解决
    metaObject.setValue("updateUser",  BaseContext.getCurrentId());


    }

    //// 更新操作，自动填充
    @Override
    public void updateFill(MetaObject metaObject) {   // 执行修改操作的时候会到这里来

//        long id = Thread.currentThread().getId();
//        log.info("线程id为：{}",id);
        log.info("线程id为：{}",BaseContext.getCurrentId());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }
}
