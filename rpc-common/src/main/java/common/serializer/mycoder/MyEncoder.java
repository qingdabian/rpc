package common.serializer.mycoder;

import common.message.MessageType;
import common.message.RpcRequest;
import common.serializer.myserializer.Serializer;
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
