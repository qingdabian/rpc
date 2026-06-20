package com.crabapple.core.server.netty;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartbeatHandler extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        try{
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                IdleState idleState=idleStateEvent.state();
                if(idleState==IdleState.READER_IDLE){
                    log.info("超过10秒没有收到客户端心跳，关闭连接");
                    ctx.close();
                }else if(idleState==IdleState.WRITER_IDLE){
                    log.info("超过10秒没有收到客户端心跳，关闭连接");
                    ctx.close();
                }
            }
        }catch (Exception e){
            log.error("心跳处理异常",e);
        }
    }
}
