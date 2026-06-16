package com.crabapple.provider.impl;



import com.crabapple.pojo.User;
import com.crabapple.service.UserService;

import java.util.Random;
import java.util.UUID;

public class UserServiceImpl implements UserService {

    @Override
    public User getUserById(Integer id) {
        System.out.println("查询了id为" + id + "的用户");
        Random random = new Random();
        User user=User.builder()
                .id(id)
                .name("用户" + UUID.randomUUID().toString())
                .age(random.nextInt(0,2)).build();
        return user;
    }

    @Override
    public Integer insertUser(User user) {
        System.out.println("插入了id为" + user.getId() + "的用户");
        return user.getId();
    }
}
