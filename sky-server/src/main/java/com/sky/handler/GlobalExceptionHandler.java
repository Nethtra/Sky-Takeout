package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获BaseException
     *
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex) {
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 1.1捕获sql异常
     *
     * @param ex
     * @return
     */
    //BaseException继承自RuntimeException 看这个Exception继承自SQLException，不是一路的，所以要再一个异常处理器
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        String message = ex.getMessage();//拿到错误信息
        //先模拟一边错误  拿到异常类型写上  异常信息写上
        //SQLIntegrityConstraintViolationException: Duplicate entry 'asdg' for key 'employee.idx_username'] with root cause
        //用户名重复的情况
        if (message.contains("Duplicate entry")) {
            String[] split = message.split(" ");//分片  注意分隔符是一个空格
            String username = split[2];
            String msg = username + MessageConstant.ALREADY_EXISTS;//xx用户已存在
            log.error("异常：{}",message);
            return Result.error(msg);
        } else {
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }

}
