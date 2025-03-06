package com.itheima.controller;

import com.itheima.entity.User;
import com.itheima.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserMapper userMapper;

    //1
    //CachePut 在原始方法执行后将方法的返回值作为v放入缓存中 注意是返回值当v
    //这么看所以 注解要加在controller 因为要缓存的是返回给前端的数据
    //cacheNames 起名要与业务相关  这两个字段决定了在redis中保存的缓存的key的名称 userCache::1(假设1是user.id)
    //redis支持树形结构   观察redis的保存结构userCache::1是key的名称 且有树形结构 以:分隔
    //key可以使用以#开头的  SpEL表达式   动态生成key的值  如果不动态生成那每次的key都是一样的 覆盖了
    @CachePut(cacheNames = "userCache", key = "#user.id")//这一个user代表方法中的形参
//    @CachePut(cacheNames = "userCache", key = "#result.id")//这个result是return 的那个user
//    @CachePut(cacheNames = "userCache", key = "#p0.id")//0第1个参数
//    @CachePut(cacheNames = "userCache", key = "#a0.id")//同上
//    @CachePut(cacheNames = "userCache", key = "#root.args[0]")//同上
    //mapper里@Options(useGeneratedKeys = true,keyProperty = "id")拿回了主键值
    @PostMapping
    public User save(@RequestBody User user) {
        userMapper.insert(user);
        return user;
    }

    //2
    //Cacheable  先查缓存，有就直接返回，没有就查数据库 然后保存到缓存里
    //原理使用了代理技术  springCache会创建controller的代理对象在原始方法执行前先用代理对象查redis缓存
    //如果缓存有就直接返回  不会再调用原始方法  如果没有缓存就会通过反射调用原始方法  然后执行结束后新增缓存
    //如果redis中有缓存  debug可以看到断点根本不会执行就能拿到数据
    //还发现一个问题如果查缓存中没有的而且数据库中也没有的比如id=100 也会在redis中创建缓存 只不过内容挺空
    //其实也没有影响  再次插入时上面的@CachePut会覆盖缓存  但是把@CachePut注释起来后  再插入  就会查不到  感觉有点问题
    //所以说CachePut和Cacheable需要配合出现
    //想了想其实也不用  只要CacheEvict一下就可以了  虽然查没有的id会创建一个空的缓存 但是只要插入时先删除缓存 然后下次Cacheable就会把新的放进去
    @Cacheable(cacheNames = "userCache", key = "#id")//这个id就是方法形参里的id
    @GetMapping
    public User getById(Long id) {
        User user = userMapper.getById(id);
        return user;
    }

    //3
    //@CacheEvict删除缓存数据  原理也是通过代理对象  在原始方法执行完之后删除redis缓存
    @CacheEvict(cacheNames = "userCache", key = "#id")
    @DeleteMapping
    public void deleteById(Long id) {
        userMapper.deleteById(id);
    }

    //上面只是删除一条，如果要全删
    //删除userCache键值对下的所有缓存 allEntries = true
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    @DeleteMapping("/delAll")
    public void deleteAll() {
        userMapper.deleteAll();
    }

}
