package org.jeecf.kong.rpc.protocol;

import org.jeecf.kong.rpc.common.SpringContextUtils;
import org.jeecf.kong.rpc.exchange.ServerDispather;
import org.jeecf.kong.rpc.protocol.serializer.MsgProtocol;
import org.jeecf.kong.rpc.register.KrpcServerProperties;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 服务处理类
 * 
 * @author jianyiming
 *
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {

    private KrpcServerProperties properties = SpringContextUtils.getBean(KrpcServerProperties.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof MsgProtocol)) {
            return;
        }
        MsgProtocol protocol = ((MsgProtocol) msg);
        ServerDispather.getInstance(properties.getThread()).dispath(protocol.getContent(), ctx, protocol.getSerializer());
    }

}
