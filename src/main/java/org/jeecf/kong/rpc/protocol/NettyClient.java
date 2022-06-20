package org.jeecf.kong.rpc.protocol;

import java.lang.Thread.State;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import javax.net.ssl.SSLEngine;

import org.jeecf.kong.rpc.discover.ContextContainer;
import org.jeecf.kong.rpc.discover.ContextEntity;
import org.jeecf.kong.rpc.protocol.serializer.MsgDecoder;
import org.jeecf.kong.rpc.protocol.serializer.MsgEncoder;
import org.jeecf.kong.rpc.protocol.serializer.MsgProtocol;
import org.jeecf.kong.rpc.protocol.serializer.Request;
import org.jeecf.kong.rpc.protocol.serializer.Serializer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 通信客户端
 * 
 * @author jianyiming
 *
 */
public class NettyClient {

    private Bootstrap bootstrap = null;

    private Channel ch = null;

    public NettyClient(int writeTime, int low, int hegith, SSLEngine engine) {
        WriteBufferWaterMark writeBufferWaterMark = new WriteBufferWaterMark(low, hegith);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup).option(ChannelOption.WRITE_BUFFER_WATER_MARK, writeBufferWaterMark).channel(NioSocketChannel.class);

        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {

            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                if (engine != null) {
                    ch.pipeline().addFirst("ssl", new SslHandler(engine));
                }
                ch.pipeline().addLast(new MsgDecoder());
                ch.pipeline().addLast(new MsgEncoder());
                ch.pipeline().addLast(new IdleStateHandler(0, writeTime, 0, TimeUnit.SECONDS));
                ch.pipeline().addLast(new HeartBeatHandler());
                ch.pipeline().addLast(new ClientHandler());
            }
        });
    }

    public synchronized boolean connection(String host, int port) throws InterruptedException {
        if (ch == null || !ch.isActive())
            ch = bootstrap.connect(host, port).sync().channel();
        return true;
    }

    public boolean send(String host, int port, Request req, Serializer serializer) throws InterruptedException {
        boolean isConnection = this.connection(host, port);
        if (!isConnection || !ch.isWritable()) {
            return false;
        }
        MsgProtocol msg = new MsgProtocol();
        byte[] content = Serializer.getSerializer(serializer, req);
        msg.setSerializer(Serializer.getSerializer(serializer));
        msg.setContentLength(content.length);
        msg.setContent(content);
        ChannelFuture clientFuture = ch.writeAndFlush(msg);
        clientFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    ContextEntity entity = ContextContainer.getInstance().get(req.getClientSpan());
                    if (entity != null && entity.getThread() != null) {
                        Thread t = entity.getThread();
                        if (t.getState().equals(State.TIMED_WAITING) || t.getState().equals(State.WAITING))
                            LockSupport.unpark(entity.getThread());
                    }
                }
            }
        });
        return true;
    }

    public void close() {
        if (ch != null) {
            ch.close();
            ch = null;
        }
        if (bootstrap != null) {
            bootstrap = null;
        }
    }

}
