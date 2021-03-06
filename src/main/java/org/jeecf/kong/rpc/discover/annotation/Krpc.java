package org.jeecf.kong.rpc.discover.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解 客户端
 * 
 * @author jianyiming
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Krpc {
    /**
     * 路径
     * 
     * @return
     */
    public String value();

    /**
     * 版本
     * 
     * @return
     */
    public int version() default 0;

    /**
     * 重试次数
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

    /**
     * 同步
     * 
     * @return
     */
    public boolean sync() default true;

}
