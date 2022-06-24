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
     * @return 访问服务端路径定位符的基础路径
     */
    public String value() default "";

    /**
     * 别名
     * 
     * @return 别名，区分多个服务
     */
    public String alias() default "";

    /**
     * 版本
     * 
     * @return 版本号与服务端对齐
     */
    public int version() default 0;

    /**
     * 重试
     * 
     * @return 重试次数
     */
    public int retry() default 2;

    /**
     * 超时时间 
     * 
     * @return 响应超时时间
     */
    public int timeout() default 3000;
    /**
     * 回调路径 完全限定类名
     * 
     * @return 降级服务的完全限定类名
     */
    public String fallback() default "";

}
