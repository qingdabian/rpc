package com.crabapple.core.server.server;

import com.crabapple.core.server.provider.ServiceProvider;

import common.message.RpcRequest;
import common.message.RpcResponse;
import lombok.AllArgsConstructor;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.Socket;

@AllArgsConstructor
public class WorkThread implements Runnable{
    private Socket socket;
    private ServiceProvider services;
    @Override
    public void run() {
        try{
            ObjectOutputStream oos=new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois=new ObjectInputStream(socket.getInputStream());
            RpcRequest request=(RpcRequest) ois.readObject();
            RpcResponse response=getResponse(request);
            oos.writeObject(response);
            oos.flush();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public RpcResponse getResponse(RpcRequest request){
        String interfacename=request.getInterfacename();
        Object service=services.getService(interfacename);
        Method method=null;
        try{
            method=service.getClass().getMethod(request.getMethodname(),request.getParamTypes());
            Object result=method.invoke(service,request.getParams());
            return RpcResponse.success(result);
        }catch(Exception e){
            e.printStackTrace();
            return RpcResponse.error();
        }
    }
}
