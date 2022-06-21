package org.jeecf.kong.rpc.exchange;

import java.util.Set;

/**
 * 
 * 重试管理器
 * 
 * @author jianyiming
 *
 */
public abstract class RetryManager {

    protected Set<Class<? extends Exception>> retryExceptions = getRetryExceptions();

    public boolean isRetry(Class<? extends Exception> e) {
        return retryExceptions.contains(e);
    }

    public abstract Set<Class<? extends Exception>> getRetryExceptions();

}
