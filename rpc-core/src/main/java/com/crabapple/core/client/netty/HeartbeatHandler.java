package com.crabapple.core.client.netty;

import common.message.RpcRequest;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartbeatHandler extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                IdleState idleState=idleStateEvent.state();
                if(idleState==IdleState.WRITER_IDLE){
                    log.info("超过10秒没有收到客户端心跳，关闭连接");
                    ctx.writeAndFlush(RpcRequest.heartBeat());
                }
            }else{
                super.userEventTriggered(ctx, evt);
            }
    }
}
