package org.jeecf.kong.rpc.protocol.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
/**
 * 信息编码 magic(4) + version(1) + serializer(1)+contentLength(4)+content
 * 
 * @author jianyiming
 *
 */
public class MsgEncoder extends MessageToByteEncoder<MsgProtocol> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MsgProtocol msg, ByteBuf out) throws Exception {
        out.writeInt(msg.getMagic());
        out.writeByte(msg.getVersion());
        out.writeByte(msg.getSerializer());
        out.writeInt(msg.getContentLength());
        out.writeBytes(msg.getContent());
        ctx.flush();
    }

}
