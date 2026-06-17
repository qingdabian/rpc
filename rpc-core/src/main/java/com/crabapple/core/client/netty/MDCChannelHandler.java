package com.crabapple.core.client.netty;

import common.trace.TraceContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;

import java.util.Map;

public class MDCChannelHandler extends ChannelOutboundHandlerAdapter {
public static final AttributeKey<Map<String,String>> TRACE_CONTEXT_KEY=AttributeKey.valueOf("traceContext");
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        Map<String,String> traceContext=ctx.channel().attr(TRACE_CONTEXT_KEY).get();
        if(traceContext!=null){
            TraceContext.clone(traceContext);
        }
        super.write(ctx,msg,promise);
    }

}
