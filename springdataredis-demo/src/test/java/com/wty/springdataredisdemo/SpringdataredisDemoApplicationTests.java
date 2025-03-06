package com.wty.springdataredisdemo;

import com.wty.springdataredisdemo.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 测试java操作redis的客户端SpringDataRedis
 */
@SpringBootTest
class SpringdataredisDemoApplicationTests {
//    @Autowired
//    private RedisTemplate redisTemplate;
//
//    @Test
//    public void testRedisTemplate() {
//        redisTemplate.opsForValue().set("name", "hello");
//        String name = (String) redisTemplate.opsForValue().get("name");
//        System.out.println(name);
//    }

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置序列化器后的测试
     */
    @Test
    public void testRedisTemplate() {
        redisTemplate.opsForValue().set("name", "hello");
        String name = (String) redisTemplate.opsForValue().get("name");
        System.out.println(name);
    }

    /**
     * 测试value为对象时能否成功序列化
     */
    @Test
    public void testObjectToRedis() {
        User user = new User("张三", 20);
        redisTemplate.opsForValue().set("zhangsan", user);//序列化
        User zhangsan = (User) redisTemplate.opsForValue().get("zhangsan");//反序列化
        System.out.println(zhangsan);
    }
}
