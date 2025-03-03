package com.sky.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 测试SpringDataRedis
 * 测试类需要与配置文件类在同一层级
 *
 * @author 王天一
 * @version 1.0
 */
@SpringBootTest
public class SpringDataRedisTest {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testRedisTemplate() {
        System.out.println(redisTemplate);
        ValueOperations valueOperations = redisTemplate.opsForValue();//string
        HashOperations hashOperations = redisTemplate.opsForHash();//hash
        ListOperations listOperations = redisTemplate.opsForList();//list
        SetOperations setOperations = redisTemplate.opsForSet();//set
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();//zset
    }

    /**
     * 测试java操作redis的string
     */
    @Test
    public void testString() {
        //set get setex setnx
        //因为没给泛型 所以set方法可以传object类型  会自动序列化为redis的string
        redisTemplate.opsForValue().set("city", "上海");
        String city = (String) redisTemplate.opsForValue().get("city");//没有设置泛型就要强转
        System.out.println(city);
        redisTemplate.opsForValue().set("code", "1234", 1, TimeUnit.MINUTES);//setex TimeUnit枚举类 时间的单位
        redisTemplate.opsForValue().setIfAbsent("city", "伤害");
    }

    /**
     * 测试java操作redis的hash
     */
    @Test
    public void testHash() {
        //hset hget hkeys hvals hdel
        //用一个操作对象来操作   指定泛型
        //注意参数的标识  key   hashkey   value
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        hashOperations.put("1", "name", "tom");//hset
        hashOperations.put("1", "age", "10");//hset
        String name = hashOperations.get("1", "name");//hget
        System.out.println(name);
        Set<String> hashKeys = hashOperations.keys("1");//hkeys
        System.out.println(hashKeys);
        List<String> values = hashOperations.values("1");//hvals
        System.out.println(values);
        hashOperations.delete("1", "age");
    }

    /**
     * 测试java操作redis的list
     */
    @Test
    public void testList() {
        //lpush lrange rpop llen
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        listOperations.leftPushAll("mylist", "a", "b", "c");//lpush多个
        listOperations.leftPush("mylist", "d");//lpush一个
        List<String> mylist = listOperations.range("mylist", 0, -1);//lrange
        System.out.println(mylist);
        Long len = listOperations.size("mylist");//llen
        System.out.println(len);
        listOperations.rightPop("mylist");//rpop
    }

    /**
     * 测试java操作redis的set
     */
    @Test
    public void testSet() {
        //sadd smembers scard sinter sunion srem
        SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        setOperations.add("set1", "a", "b", "c", "d");//saaa
        setOperations.add("set2", "a", "b", "x", "y");
        Set<String> set1 = setOperations.members("set1");//smembers
        System.out.println(set1);
        Long set2 = setOperations.size("set2");//scard
        System.out.println(set2);
        Set<String> intersect = setOperations.intersect("set1", "set2");
        System.out.println("交集：" + intersect);//sinter
        Set<String> union = setOperations.union("set1", "set2");
        System.out.println("并集：" + union);//sunion
        setOperations.remove("set2", "a");//srem
    }

    /**
     * 测试java操作redis的zset
     */
    @Test
    public void testZset() {
        //zadd zrange zincrby zrem
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.add("zset1", "a", 10);//zadd
        zSetOperations.add("zset1", "b", 15);
        zSetOperations.add("zset1", "c", 20);
        Set<String> zset1 = zSetOperations.range("zset1", 0, -1);//zrange
        System.out.println(zset1);
        zSetOperations.incrementScore("zset1", "a", 8);//zincrby
        zSetOperations.remove("zset1", "a");//zrem
    }

    /**
     * 测试java操作redis的通用命令
     */
    @Test
    public void testCommon() {
        //keys exists type del
        //通用命令直接使用RedisTemplate对象来操作
        Set keys = redisTemplate.keys("*");
        System.out.println(keys);
        Boolean name = redisTemplate.hasKey("name");
        System.out.println(name);
        for (Object key : keys) {
            DataType type = redisTemplate.type(key);
            System.out.println("类型：" + type);
        }
//        redisTemplate.delete("mylist");
    }
}
