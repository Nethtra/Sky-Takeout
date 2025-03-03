package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 测试Spring task
 *
 * @author 王天一
 * @version 1.0
 */
@Component//要交给Spring容器管理
@Slf4j
public class MyTaskTest {
    @Scheduled(cron = "0-0 * * * * ?")//使用@Scheduled注解指定cron表达式
    public void executeTask() {
        log.info("Spring Task执行{}", LocalDateTime.now());//业务逻辑
    }//启动项目后就会执行
}
