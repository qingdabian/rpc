package com.crabapple.concumer;


import com.crabapple.core.client.proxy.ClientProxy;
import com.crabapple.core.trace.ZipkinReporter;
import com.crabapple.pojo.User;
import com.crabapple.service.UserService;

import java.io.IOException;

public class TestClient {
    public static void main(String[] args) throws InterruptedException, IOException {

        // 不依赖任何 Reporter，直接 HTTP POST
        String json = "[{\"traceId\":\"051977ee11c00000\",\"id\":\"9999999999999999\","
                + "\"kind\":\"CLIENT\",\"name\":\"direct-http-test\","
                + "\"timestamp\":" + (System.currentTimeMillis() * 1000L) + ","
                + "\"duration\":1000000,"
                + "\"localEndpoint\":{\"serviceName\":\"direct-test\"}}]";

        java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("http://localhost:9411/api/v2/spans"))
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                .build();

        java.net.http.HttpResponse<String> resp = java.net.http.HttpClient.newHttpClient()
                .send(req, java.net.http.HttpResponse.BodyHandlers.ofString());

        System.out.println("HTTP 状态码: " + resp.statusCode());
        System.out.println("HTTP 响应: " + resp.body());


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

        // 1. 给 AsyncReporter 时间 flush
        Thread.sleep(3000);

// 2. 手动 close 触发 flush
        ZipkinReporter.close();
    }
}
