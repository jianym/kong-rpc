package org.jeecf.kong.rpc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jeecf.kong.rpc.discover.ConsumerDiscover;
import org.jeecf.kong.rpc.exchange.BasicRetryManager;
import org.jeecf.kong.rpc.exchange.RetryManager;
import org.jeecf.kong.rpc.exchange.SslSocketEngine;
import org.springframework.context.annotation.Import;

/**
 * 服务发现
 * 
 * @author jianyiming
 *
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ConsumerDiscover.class)
public @interface EnableKrpcDiscover {

    /**
     * 重试管理器
     * 
     * @return
     */
    public Class<? extends RetryManager> retryManager() default BasicRetryManager.class;

    /**
     * ssl引擎
     * 
     * @return
     */
    public Class<? extends SslSocketEngine> sslEngine() default SslSocketEngine.class;

}
