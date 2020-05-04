package org.jeecf.kong.rpc.discover.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解客户端
 * 
 * @author jianyiming
 *
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface KrpcClient {
    /**
     * 基础路径
     * 
     * @return
     */
    public String value() default "";

    /**
     * 别名
     * 
     * @return
     */
    public String alias() default "";

    /**
     * 版本
     * 
     * @return
     */
    public int version() default 0;

    /**
     * 重试
     * 
     * @return
     */
    public int retry() default 2;

    /**
     * 超时时间
     * 
     * @return
     */
    public int timeout() default 3000;

}
