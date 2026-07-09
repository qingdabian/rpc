package com.crabapple.core.client.netty;


import common.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
    //配置attributekey存储响应数据
        AttributeKey<RpcResponse> attributeKey=AttributeKey.valueOf("RPCResponse");
        channelHandlerContext.attr(attributeKey).set(rpcResponse);
        channelHandlerContext.channel().close();
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {
        throwable.printStackTrace();
        channelHandlerContext.close();
    }
}
