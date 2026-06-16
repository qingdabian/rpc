package com.crabapple.core.server.server;

import com.crabapple.core.server.provider.ServiceProvider;
import lombok.extern.slf4j.Slf4j;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ThreadPoolServer implements RpcServer{
    private ServiceProvider services;
    private ThreadPoolExecutor threadPoolExecutor;
    public ThreadPoolServer(ServiceProvider services){
        this.services=services;
        threadPoolExecutor=new ThreadPoolExecutor(10,20,60, TimeUnit.SECONDS,new ArrayBlockingQueue<>(100));
    }

    @Override
    public void start(int port) {
        try{
            ServerSocket serverSocket=new ServerSocket(port);
            log.debug("服务器启动成功，端口号：{}",port);
            while(true){
                Socket socket=serverSocket.accept();
                threadPoolExecutor.submit(new WorkThread(socket,services));
            }
        }catch(Exception e){
            e.printStackTrace();
            log.debug("服务器启动异常");
        }
    }

    @Override
    public void stop() {

    }
}
