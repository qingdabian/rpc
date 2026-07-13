package com.crabapple.core.client.proxy;

import com.crabapple.core.client.circuitbreaker.CircuitBreakProvider;
import com.crabapple.core.client.circuitbreaker.CircuitBreaker;
import com.crabapple.core.client.servicecenter.ServiceCenter;
import com.crabapple.core.client.servicecenter.impl.ZKServiceCenter;
import com.crabapple.core.client.retry.GuavaRetry;
import com.crabapple.core.client.rpcclient.RpcClient;
import com.crabapple.core.client.rpcclient.impl.SimpleRpcClient;
import com.crabapple.core.client.rpcclient.impl.NettyRpcClient;
import com.crabapple.core.trace.interceptor.ClientTraceInterceptor;
import common.message.RpcRequest;
import common.message.RpcResponse;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;


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
        ClientTraceInterceptor.beforeInvoke();
        RpcResponse response;
        RpcRequest request= RpcRequest.builder()
                .requestId(UUID.randomUUID().toString())
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
            response= new GuavaRetry().sendServiceWithRetry(request,rpcClient);
        }else{
            response= rpcClient.sendRpcRequest(request);
        }
        if(response!=null){
            if(response.getCode()==200){
                circuitBreaker.recordSuccess();
            }else if(response.getCode()==500){
                circuitBreaker.recordFailure();
            }
        }
        ClientTraceInterceptor.afterInvoke(method.getName());
        return response!=null?response.getData():null;
    }

    public <T> T getProxy(Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),new Class[]{clazz},this);
    }
    public void close(){
        rpcClient.close();
        serviceCenter.close();
    }
}
