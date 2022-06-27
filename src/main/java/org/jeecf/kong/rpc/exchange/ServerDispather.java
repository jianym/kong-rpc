package org.jeecf.kong.rpc.exchange;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jeecf.kong.rpc.register.KrpcServerProperties.ThreadProperties;

import com.google.protobuf.InvalidProtocolBufferException;

import io.netty.channel.ChannelHandlerContext;

/**
 * 服务分发
 * 
 * @author jianyiming
 *
 */
public class ServerDispather {
    /**
     * 线程池
     */
    private static ThreadPoolExecutor executorService = null;

    private static volatile ServerDispather providerDispather = null;
    
    private ServerDispather() {
    };

    public static ServerDispather getInstance(ThreadProperties thread) {
        if (providerDispather != null) {
            return providerDispather;
        }
        synchronized (ServerDispather.class) {
            if (providerDispather != null) {
                return providerDispather;
            }
            executorService = new ThreadPoolExecutor(thread.getCore(),thread.getCore(),0,TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(thread.getQueue()));
            return providerDispather = new ServerDispather();
        }
    }

    public void dispath(byte[] request, ChannelHandlerContext ctx, byte serializer) throws InvalidProtocolBufferException {
        executorService.execute(new DispatchTask(request, ctx, serializer));
    }

}
