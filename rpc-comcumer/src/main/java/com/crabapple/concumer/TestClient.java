package com.crabapple.concumer;


import com.crabapple.core.client.proxy.ClientProxy;
import com.crabapple.pojo.User;
import com.crabapple.service.UserService;

public class TestClient {
    public static void main(String[] args) {
        ClientProxy clientProxy=new ClientProxy(0);
        UserService userService=clientProxy.getProxy(UserService.class);

        User user=userService.getUserById(1);
        System.out.println(user);

        User user1=User.builder()
                .id(2)
                .name("用户2")
                .age(20)
                .build();
        System.out.println(userService.insertUser(user1));
    }
}
