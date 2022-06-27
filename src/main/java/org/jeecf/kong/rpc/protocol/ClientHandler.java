package org.jeecf.kong.rpc.protocol;

import java.util.concurrent.locks.LockSupport;

import org.jeecf.kong.rpc.discover.ContextContainer;
import org.jeecf.kong.rpc.discover.ContextEntity;
import org.jeecf.kong.rpc.protocol.serializer.MsgProtocol;
import org.jeecf.kong.rpc.protocol.serializer.ResponseSerializerHelper;

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
        ResponseSerializerHelper responseHelper = new ResponseSerializerHelper( protocol.getContent(),protocol.getSerializer());
        String span = responseHelper.getClientSpan();
        ContextEntity entity = ContextContainer.getInstance().get(span);
        if (entity != null ) {
            entity.setResponseHelper(responseHelper);
            LockSupport.unpark(entity.getThread());
        }
    }

}
