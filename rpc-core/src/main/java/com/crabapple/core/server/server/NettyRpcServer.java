package com.crabapple.core.server.server;


import com.crabapple.core.server.netty.NettyServerInitializer;
import com.crabapple.core.server.provider.ServiceProvider;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class NettyRpcServer implements RpcServer {
    private ServiceProvider serviceProvider;
    private ChannelFuture channelFuture;
    public NettyRpcServer(ServiceProvider serviceProvider){
        this.serviceProvider=serviceProvider;
    }
    @Override
    public void start(int port) {
        NioEventLoopGroup bossGroup=new NioEventLoopGroup();
        NioEventLoopGroup workerGroup=new NioEventLoopGroup();
        ServerBootstrap serverBootstrap=new ServerBootstrap();
        log.info("NettyRpcServer start port:{}",port);
        try{
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyServerInitializer(serviceProvider));
            channelFuture=serverBootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
            Thread.currentThread().interrupt();
            log.error("NettyRpcServer start error",e);
        }
        finally{
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    @Override
    public void stop() {
        if(channelFuture!=null){
            try{
                channelFuture.channel().close().sync();
                log.info("Netty主通道已经关闭");
            }catch (InterruptedException e){
                Thread.currentThread().interrupt();
                log.error("关闭主通道的服务中断");
            }
        }else{
            log.info("NettyRpcServer 未启动,无法关闭");
        }
    }

    private void shutdown(NioEventLoopGroup bossGroup,NioEventLoopGroup workerGroup){
        if(bossGroup!=null){
            bossGroup.shutdownGracefully().syncUninterruptibly();
        }
        if(workerGroup!=null){
            workerGroup.shutdownGracefully().syncUninterruptibly();
        }
    }
}
