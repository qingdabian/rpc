package com.crabapple.provider.impl;

import com.crabapple.core.RpcApplication;
import com.crabapple.core.server.provider.ServiceProvider;
import com.crabapple.core.server.server.NettyRpcServer;
import com.crabapple.core.server.server.RpcServer;
import com.crabapple.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProviderTest {
    public static void main(String[] args) throws InterruptedException {
        RpcApplication.init();
        String ip=RpcApplication.getRpcConfig().getHost();
        int port=RpcApplication.getRpcConfig().getPort();
        // 创建 UserService 实例
        UserService userService = new UserServiceImpl();
        ServiceProvider serviceProvider = new ServiceProvider(ip, port);
        // 发布服务接口到 ServiceProvider
        serviceProvider.registerService(userService,true);  // 可以设置是否支持重试

        // 启动 RPC 服务器并监听端口
        RpcServer rpcServer = new NettyRpcServer(serviceProvider);
        rpcServer.start(port);  // 启动 Netty RPC 服务，监听 port 端口
        log.info("RPC 服务端启动，监听端口" + port);
    }
}
