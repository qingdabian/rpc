package com.crabapple.core.client.proxy;

import com.crabapple.core.client.circuitbreaker.CircuitBreakProvider;
import com.crabapple.core.client.circuitbreaker.CircuitBreaker;
import com.crabapple.core.client.servicecenter.ServiceCenter;
import com.crabapple.core.client.servicecenter.impl.ZKServiceCenter;
import com.crabapple.core.client.retry.GuavaRetry;
import com.crabapple.core.client.rpcclient.RpcClient;
import com.crabapple.core.client.rpcclient.impl.SimpleRpcClient;
import com.crabapple.core.client.rpcclient.impl.NettyRpcClient;
import common.message.RpcRequest;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


public class ClientProxy implements InvocationHandler {
    private String host;
    private int port;
    private RpcClient rpcClient;
    private ServiceCenter serviceCenter=new ZKServiceCenter();
    private CircuitBreakProvider circuitBreakProvider=new CircuitBreakProvider();
    public ClientProxy(int choose){
        switch (choose){
            case 0:
                rpcClient=new NettyRpcClient();
                break;
            case 1:
                rpcClient=new SimpleRpcClient();
                break;
        }
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        RpcRequest request= RpcRequest.builder()
                .interfacename(method.getDeclaringClass().getName())
                .methodname(method.getName())
                .params(args)
                .paramTypes(method.getParameterTypes())
                .build();
        CircuitBreaker circuitBreaker=circuitBreakProvider.getCircuitBreaker(request.getInterfacename());
        if(!circuitBreaker.allowRequest()){
            return null;
        }
        if(serviceCenter.checkRetry(request.getInterfacename())){
            return new GuavaRetry().sendServiceWithRetry(request,rpcClient).getData();
        }else{
            return rpcClient.sendRpcRequest(request).getData();
        }
    }

    public <T> T getProxy(Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),new Class[]{clazz},this);
    }
    public void close(){
        rpcClient.close();
        serviceCenter.close();
    }
}
