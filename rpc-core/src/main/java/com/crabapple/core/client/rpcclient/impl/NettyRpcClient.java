package com.crabapple.core.client.rpcclient.impl;



import com.crabapple.core.client.netty.MDCChannelHandler;
import com.crabapple.core.client.netty.NettyClientInitializer;
import com.crabapple.core.client.rpcclient.RpcClient;
import com.crabapple.core.client.servicecenter.ServiceCenter;
import com.crabapple.core.client.servicecenter.impl.ZKServiceCenter;
import common.message.RpcRequest;
import common.message.RpcResponse;
import common.trace.TraceContext;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class NettyRpcClient implements RpcClient {
    private static final Bootstrap bootstrap;
    public static final EventLoopGroup eventLoopGroup;
    private ServiceCenter serviceCenter;

    /** 每个地址的连接池大小 */
    private static final int POOL_SIZE = 32;
    /** 地址 → Channel 数组 */
    private static final ConcurrentHashMap<InetSocketAddress, Channel[]> channelPool
            = new ConcurrentHashMap<>();
    /** 地址 → 轮询计数器 */
    private static final ConcurrentHashMap<InetSocketAddress, AtomicInteger> roundRobin
            = new ConcurrentHashMap<>();
    /** requestId → 等待响应的 Future */
    public static final ConcurrentHashMap<String, CompletableFuture<RpcResponse>> pendingRequests
            = new ConcurrentHashMap<>();
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
        Map<String,String> traceContext= TraceContext.getCopy();
        try{
            InetSocketAddress addr=serviceCenter.serviceDicovery(request.getInterfacename());

            // 从多连接池中轮询选一个 Channel
            Channel channel = selectChannel(addr);
            channel.attr(MDCChannelHandler.TRACE_CONTEXT_KEY).set(traceContext);

            // 确保有 requestId
            if (request.getRequestId() == null) {
                request.setRequestId(UUID.randomUUID().toString());
            }
            String requestId = request.getRequestId();

            // 注册 Future，等待响应
            CompletableFuture<RpcResponse> future = new CompletableFuture<>();
            pendingRequests.put(requestId, future);

            channel.writeAndFlush(request).sync();

            try {
                return future.get(5, TimeUnit.SECONDS);
            } finally {
                pendingRequests.remove(requestId);
            }

        }catch (Exception e){
            log.error("sendRpcRequest error", e);
        }
        return null;
    }

    /** 从连接池中轮询选取 Channel，断线自动重连 */
    private Channel selectChannel(InetSocketAddress addr) {
        Channel[] channels = channelPool.computeIfAbsent(addr, k -> {
            Channel[] arr = new Channel[POOL_SIZE];
            for (int i = 0; i < POOL_SIZE; i++) {
                try {
                    arr[i] = bootstrap.connect(k).sync().channel();
                } catch (Exception e) {
                    throw new RuntimeException("创建连接失败: " + k, e);
                }
            }
            return arr;
        });

        AtomicInteger counter = roundRobin.computeIfAbsent(addr,
                k -> new AtomicInteger(0));
        int index = counter.getAndIncrement() % POOL_SIZE;
        if (index < 0) index += POOL_SIZE;

        // 断线重连（双重检查）
        Channel channel = channels[index];
        if (!channel.isActive()) {
            synchronized (channels) {
                if (!channels[index].isActive()) {
                    try {
                        channels[index] = bootstrap.connect(addr).sync().channel();
                    } catch (Exception e) {
                        throw new RuntimeException("重连失败: " + addr, e);
                    }
                }
            }
            channel = channels[index];
        }
        return channel;
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
