package org.jeecf.kong.rpc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jeecf.kong.rpc.register.ProviderRegister;
import org.springframework.context.annotation.Import;

/**
 * 服务注册
 * 
 * @author jianyiming
 *
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ProviderRegister.class)
public @interface EnableKrpcRegister {

}
