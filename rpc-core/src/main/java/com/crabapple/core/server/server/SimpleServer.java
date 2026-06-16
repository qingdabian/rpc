package com.crabapple.core.server.server;

import com.crabapple.core.server.provider.ServiceProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
@AllArgsConstructor
public class SimpleServer implements RpcServer{
    private ServiceProvider services;

    @Override
    public void start(int port) {
        try{
            ServerSocket serverSocket=new ServerSocket(port);
            log.debug("服务器启动成功，端口号：{}",port);
            while(true){
                Socket socket=serverSocket.accept();
                WorkThread workThread=new WorkThread(socket,services);
                new Thread(workThread).start();
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
