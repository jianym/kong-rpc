package org.jeecf.kong.rpc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jeecf.kong.rpc.exchange.SslServerSocketEngine;
import org.jeecf.kong.rpc.register.ProviderRegister;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

/**
 * 服务注册
 * 
 * @author jianyiming
 *
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@Import(ProviderRegister.class)
public @interface EnableKrpcRegister {
    /**
     * ssl引擎
     * 
     * @return
     */
    public Class<? extends SslServerSocketEngine> sslEngine() default SslServerSocketEngine.class;

}
