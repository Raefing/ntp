package pres.raefing.ntp.netty.base;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public class NtMessageCodec extends ByteToMessageCodec<NtMessage> {

    private int len;
    private final NtMessageSerial messageSerial = new NtMessageSerial();

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, NtMessage ntMessage, ByteBuf byteBuf) throws Exception {
        byte[] bytes = messageSerial.encode(ntMessage);
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (len > 0) {
            byte[] read = new byte[len];
            byteBuf.readBytes(read, 0, len);
            NtMessage ntMessage = messageSerial.decode(read);
            list.add(ntMessage);
            len = 0;
        } else {
            len = byteBuf.readInt();
        }
    }
}
