package com.itheima.reggie.common;


// 自定义业务异常类
public class CustomException extends RuntimeException{
    public CustomException(String message){
        // 主要传递错误信息
        super(message);
    }
}
