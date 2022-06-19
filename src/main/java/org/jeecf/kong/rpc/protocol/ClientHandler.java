package org.jeecf.kong.rpc.protocol;

import java.util.concurrent.locks.LockSupport;

import org.jeecf.kong.rpc.discover.ConsumerContainer.ServerNode;
import org.jeecf.kong.rpc.discover.ContextContainer;
import org.jeecf.kong.rpc.discover.ContextEntity;
import org.jeecf.kong.rpc.protocol.serializer.MsgProtocol;
import org.jeecf.kong.rpc.protocol.serializer.Response;
import org.jeecf.kong.rpc.protocol.serializer.Serializer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 客户端处理类
 * 
 * @author jianyiming
 *
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof MsgProtocol)) {
            return;
        }
        MsgProtocol protocol = ((MsgProtocol) msg);
        Response res = (Response) Serializer.deserialize(protocol.getSerializer(), protocol.getContent());
        String span = res.getClientSpan();
        ContextEntity entity = ContextContainer.getInstance().get(span);
        if (entity != null ) {
            entity.setResponse(res);
            LockSupport.unpark(entity.getThread());
            if(entity.getShutdown() == ServerNode.SHUT_DOWN) {
                ctx.channel().close();
            }
        }
    }

}
