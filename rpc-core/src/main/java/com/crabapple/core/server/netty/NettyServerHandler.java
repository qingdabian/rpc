package com.crabapple.core.server.netty;


import com.crabapple.core.server.provider.ServiceProvider;
import com.crabapple.core.server.ratelimit.RateLimitProvider;
import com.crabapple.core.trace.interceptor.ServerTraceInterceptor;
import common.message.RequestType;
import common.message.RpcRequest;
import common.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private ServiceProvider services;
    public NettyServerHandler(ServiceProvider services) {
        this.services=services;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        if(rpcRequest==null){
            System.out.println("rpcRequest为空");
            return;
        }
        if(rpcRequest.getType()== RequestType.HEARTBEAT){
            log.info("收到心跳包");
            return;
        }
        ServerTraceInterceptor.beforeInvoke();
        RpcResponse response=getResponse(rpcRequest);
        ServerTraceInterceptor.afterInvoke(rpcRequest.getMethodname());
        channelHandlerContext.writeAndFlush(response);
        channelHandlerContext.close();
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {
        throwable.printStackTrace();

    }

    private RpcResponse getResponse(RpcRequest rpcRequest) {
        String interfacename=rpcRequest.getInterfacename();
        Object service=services.getService(interfacename);
        RateLimitProvider rateLimitProvider=services.getRateLimitProvider();
        boolean isRateLimit=rateLimitProvider.getRateLimit(interfacename).getToken();
        if(!isRateLimit){
            System.out.println("限流");
            return RpcResponse.error();
        }
        Method method=null;
        try{

            method=service.getClass().getMethod(rpcRequest.getMethodname(),rpcRequest.getParamTypes());
            Object result=method.invoke(service,rpcRequest.getParams());
            return RpcResponse.success(result);
        }catch(Exception e){
            e.printStackTrace();
            return RpcResponse.error();
        }
    }
}
