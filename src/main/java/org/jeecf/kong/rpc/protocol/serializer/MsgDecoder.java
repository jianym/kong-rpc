package org.jeecf.kong.rpc.protocol.serializer;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 信息解码
 * 
 * @author jianyiming
 *
 */
public class MsgDecoder extends ByteToMessageDecoder {

    public final int HEAD_LENGTH = 4 + 1 + 1 + 4;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < HEAD_LENGTH) {
            return;
        }
        int beginReader;
        while (true) {
            beginReader = in.readerIndex();
            in.markReaderIndex();
            if (in.readInt() == ConstantValue.MAGIC) {
                break;
            }
            in.resetReaderIndex();
            in.readByte();
            if (in.readableBytes() < HEAD_LENGTH) {
                return;
            }
        }
        byte version = in.readByte();
        byte serializer = in.readByte();
        int length = in.readInt();
        if (in.readableBytes() < length) {
            in.readerIndex(beginReader);
            return;
        }
        byte[] content = new byte[length];
        in.readBytes(content);

        MsgProtocol protocol = new MsgProtocol();
        protocol.setVersion(version);
        protocol.setSerializer(serializer);
        protocol.setContentLength(content.length);
        protocol.setContent(content);
        out.add(protocol);
    }

}
