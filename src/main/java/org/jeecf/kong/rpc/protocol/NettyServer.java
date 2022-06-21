package org.jeecf.kong.rpc.protocol;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLEngine;

import org.jeecf.kong.rpc.protocol.properties.ServerSocketProperties;
import org.jeecf.kong.rpc.protocol.serializer.MsgDecoder;
import org.jeecf.kong.rpc.protocol.serializer.MsgEncoder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 通信服务端
 * 
 * @author jianyiming
 *
 */
public class NettyServer {

    private ServerBootstrap server = null;

    public NettyServer(ServerSocketProperties socket, SSLEngine engine) {
        WriteBufferWaterMark writeBufferWaterMark = new WriteBufferWaterMark(socket.getLow(), socket.getHeight());
        server = new ServerBootstrap();
        EventLoopGroup parentGroup = new NioEventLoopGroup();
        EventLoopGroup childGroup = new NioEventLoopGroup();
        server.group(parentGroup, childGroup).option(ChannelOption.WRITE_BUFFER_WATER_MARK, writeBufferWaterMark).option(ChannelOption.SO_BACKLOG, socket.getBack())
                .channel(NioServerSocketChannel.class);
        
        server.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                if (engine != null) {
                    ch.pipeline().addFirst("ssl", new SslHandler(engine));
                }
                ch.pipeline().addLast(new MsgDecoder());
                ch.pipeline().addLast(new MsgEncoder());
                ch.pipeline().addLast(new IdleStateHandler(socket.getTimeout(), 0, 0, TimeUnit.SECONDS));
                ch.pipeline().addLast(new HeartBeatHandler());
                ch.pipeline().addLast(new ServerHandler());
            }
        });
    }
    
    public void run(int port) {
        try {
            server.bind(port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
