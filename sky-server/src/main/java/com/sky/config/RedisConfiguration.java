package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 配置类   创建RedisTemplate对象
 *
 * @author 王天一
 * @version 1.0
 */
@Configuration
@Slf4j
public class RedisConfiguration {
    @Bean//加
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始创建RedisTemplate对象");
        RedisTemplate redisTemplate = new RedisTemplate();
        //设置redis的连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //设置redis key的序列化器   防止在redis查看数据时key乱码 只会影响显示效果  取出来数据还是正常的
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //序列化value
        //这里设置value的序列化 当存储redis时  会出现类型转换异常  所以还是不要了
//        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}










