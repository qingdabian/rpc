package common.serializer.mycoder;

import common.message.MessageType;
import common.message.RpcRequest;
import common.serializer.myserializer.Serializer;
import common.trace.TraceContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class MyEncoder extends MessageToByteEncoder {
    private Serializer serializer;
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        System.out.println("当前的traceContext中的traceid为"+TraceContext.getTraceId());
        System.out.println("当前的traceContext中的spanid为"+TraceContext.getSpanId());
        String tracemsg= TraceContext.getTraceId()+";"+TraceContext.getSpanId();
        byte[] tracemsgBytes=tracemsg.getBytes();
        byteBuf.writeInt(tracemsgBytes.length);
        byteBuf.writeBytes(tracemsgBytes);
        log.info(o.getClass().getName());
        if(o instanceof RpcRequest){
            byteBuf.writeShort(MessageType.REQUEST.getType());
        }else {
            byteBuf.writeShort(MessageType.RESPONSE.getType());
        }
        byteBuf.writeShort(serializer.getType());
        byte[] bytes=serializer.serialize(o);
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
    }
}
