package com.crabapple.core.client.rpcclient.impl;



import com.crabapple.core.client.netty.NettyClientInitializer;
import com.crabapple.core.client.rpcclient.RpcClient;
import com.crabapple.core.client.servicecenter.ServiceCenter;
import com.crabapple.core.client.servicecenter.impl.ZKServiceCenter;
import common.message.RpcRequest;
import common.message.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class NettyRpcClient implements RpcClient {
//    private String host;
//    private int port;
    private static final Bootstrap bootstrap;
    public static final EventLoopGroup eventLoopGroup;
    private ServiceCenter serviceCenter;
    public NettyRpcClient(){
        this.serviceCenter=new ZKServiceCenter();
    }
//    public NettyRpcClient(String host,int port){
//        this.host=host;
//        this.port=port;
//    }

    static{
        bootstrap=new Bootstrap();
        eventLoopGroup=new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new NettyClientInitializer());
    }

    @Override
    public RpcResponse sendRpcRequest(RpcRequest request) {
        try{
            InetSocketAddress addr=serviceCenter.serviceDicovery(request.getInterfacename());
            ChannelFuture channelFuture=bootstrap.connect(addr).sync();
            Channel channel=channelFuture.channel();
            channel.writeAndFlush(request);
            channel.closeFuture().sync();
            AttributeKey<RpcResponse> key=AttributeKey.valueOf("RPCResponse");
            RpcResponse response=channel.attr(key).get();
            System.out.println(response);
            return response;

        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() {
        try {
            if (eventLoopGroup != null) {
                eventLoopGroup.shutdownGracefully().sync();
            }
        } catch (InterruptedException e) {
            log.error("关闭 Netty 资源时发生异常: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }
}
