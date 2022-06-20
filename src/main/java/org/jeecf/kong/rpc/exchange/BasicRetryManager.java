package org.jeecf.kong.rpc.exchange;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

import io.netty.handler.timeout.TimeoutException;

/**
 * 重试管理器
 * 
 * @author jianyiming
 *
 */
public class BasicRetryManager extends RetryManager {

    @Override
    public Set<Class<? extends Exception>> getRetryExceptions() {
        Set<Class<? extends Exception>> retryExceptions = new HashSet<>();
        retryExceptions.add(SocketException.class);
        retryExceptions.add(TimeoutException.class);
        retryExceptions.add(org.jeecf.kong.rpc.common.exception.TimeoutException.class);
        retryExceptions.add(IOException.class);
        return retryExceptions;
    }

}
