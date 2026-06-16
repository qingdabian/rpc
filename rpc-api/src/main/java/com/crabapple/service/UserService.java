package com.crabapple.service;

import com.crabapple.pojo.User;

public interface UserService {
    public User getUserById(Integer id);
    public Integer insertUser(User user);
}
