package com.crabapple.core.client.netty;


import com.crabapple.core.client.rpcclient.impl.NettyRpcClient;
import common.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.CompletableFuture;

public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        String requestId = rpcResponse.getRequestId();
        if (requestId != null) {
            CompletableFuture<RpcResponse> future = NettyRpcClient.pendingRequests.get(requestId);
            if (future != null) {
                future.complete(rpcResponse);
            }
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {
        throwable.printStackTrace();
        channelHandlerContext.close();
    }
}
