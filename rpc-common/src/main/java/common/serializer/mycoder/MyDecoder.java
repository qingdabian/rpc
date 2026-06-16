package common.serializer.mycoder;

import common.message.MessageType;
import common.serializer.myserializer.Serializer;
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
}
