package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解 指定哪个方法需要执行公共字段自动填充
 *
 * @author 王天一
 * @version 1.0
 */
@Target(ElementType.METHOD)//生效范围
@Retention(RetentionPolicy.RUNTIME)//生命周期
public @interface AutoFill {
    //用一个属性指定数据库操作类型    insert和update需要进行公共字段自动填充
    //使用枚举类
    OperationType value();
}
