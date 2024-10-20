package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author 王天一
 * @version 1.0
 */
@Mapper
public interface UserMapper {

    /**
     * 根据openid查询微信用户
     *
     * @param openId
     * @return
     */
    @Select("select * from user where openid=#{openId}")
    User selectByOpenId(String openId);

    /**
     * 插入微信用户
     *
     * @param user
     */
    void insert(User user);

    /**
     * 根据id查询用户信息
     *
     * @param userId
     * @return
     */
    @Select("select * from user where id = #{id}")
    User getById(Long userId);
}
