package com.wty.springdataredisdemo;

import com.alibaba.fastjson2.JSON;
import com.wty.springdataredisdemo.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

/**
 * 测试StringRedisTemplate类 key和value的序列化器都被设置为string
 *
 * @author 王天一
 * @version 1.0
 */
@SpringBootTest
public class TestStringRedisTemplate {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     *  对对象进行手动序列化与反序列化
     */
    @Test
    public void testSerializer() {
        User user = new User("lisi", 30);//对象
        String jsonString = JSON.toJSONString(user);//手动序列化
        stringRedisTemplate.opsForValue().set("lisi", jsonString);//存入redis
        String lisiString = stringRedisTemplate.opsForValue().get("lisi");
        User lisi = JSON.parseObject(lisiString, User.class);//手动反序列化
        System.out.println(lisi);
    }

    /**
     * 测试opsForHash  字符串不需要手动
     */
    @Test
    public void testHash(){
        stringRedisTemplate.opsForHash().put("user:20","name","jerry");
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries("user:20");
        System.out.println(entries);
    }
}
