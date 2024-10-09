package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 公共字段自动填充切面类  用到的知识：自定义枚举、注解  AOP   反射
 * @author 王天一
 * @version 1.0
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    //定义切入点表达式    前置：已经自定义注解AutoFill   自定义枚举OperationType   开发完成后不要忘了在Mapper中写@AutoFill 还要把之前写的补充属性代码删了
    //两种方式同时使用来匹配  如果只使用@annotation会扫描所有方法，性能下降，使用execution来限制范围
    //注意匹配的是Mapper中的接口
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    //前置通知
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段自动填充");
        //思路：先获取到原始方法的操作类型 insert还是update  insert需要改四个字段  update需要改两个字段
        //再获取到原始方法的参数  用参数的反射来修改字段


        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//获取方法签名对象   向下转型
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获取方法上的注解对象
        OperationType operationType = autoFill.value();//获取到操作类型  这种@AutoFill(OperationType.INSERT)

        Object[] args = joinPoint.getArgs();//获取方法的参数
        if (args == null || args.length == 0) {
            return;
        }
        Object entity = args[0];//拿第一个

        LocalDateTime now = LocalDateTime.now();//准备数据
        Long id = BaseContext.getCurrentId();
        if (operationType == OperationType.INSERT) {
            //用反射来修改字段
            //因为entity是Object类型，没有getset方法，必须通过反射获得方法 还有弹幕说拿出来的entity不是new出来的对象，没法直接set 我觉得都有道理
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setCreateTime.invoke(entity, now);
                setUpdateTime.invoke(entity, now);
                setCreateUser.invoke(entity, id);
                setUpdateUser.invoke(entity, id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else if (operationType == OperationType.UPDATE) {
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
