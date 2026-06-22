package common.serializer.mycoder;

import common.message.MessageType;
import common.serializer.myserializer.Serializer;
import common.trace.TraceContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MyDecoder extends ByteToMessageDecoder {
    private Serializer serializer;
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        //读取traceId和spanId
        int tracemsgLength=byteBuf.readInt();
        byte[] tracemsgBytes=new byte[tracemsgLength];
        byteBuf.readBytes(tracemsgBytes);
        serializeTraceMsg(tracemsgBytes);

        int messageType=byteBuf.readShort();
        int serializerType=byteBuf.readShort();
        if(messageType!=MessageType.REQUEST.getType()&&messageType!= MessageType.RESPONSE.getType()){
            log.error("messageType is not request or response");
            return;
        }
        serializer=Serializer.getSerializer(serializerType);
        if(serializer==null){
            log.error("serializer is null");
            return;
        }
        int length=byteBuf.readInt();
        byte[] bytes=new byte[length];
        byteBuf.readBytes(bytes);
        Object object=serializer.deserialize(bytes,messageType);
        list.add(object);
    }

    //解析traceId和spanId,并设置到TraceContext中
    private void serializeTraceMsg(byte[] tracemsgBytes){
        String tracemsg=new String(tracemsgBytes);
        String[] traceMsg=tracemsg.split(";");
        TraceContext.setTraceId(traceMsg[0]);
        TraceContext.setParentSpanId(traceMsg[1]);
    }
}
