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
     * @return 访问路径定位符
     */
    public String value();

    /**
     * 版本
     * 
     * @return 版本号与服务端对齐
     */
    public int version() default 0;

    /**
     * 重试次数
     * 
     * @return 异常后重试次数
     */
    public int retry() default 2;

    /**
     * 超时时间
     * 
     * @return 响应超时时间
     */
    public int timeout() default 3000;

    /**
     * 同步
     * 
     * @return true同步传输，false 异步传输
     */
    public boolean sync() default true;

    /**
     * 回调路径 完全限定类名
     * 
     * @return true 支持服务降级，false 不支持服务降级
     */
    public boolean fallback() default true;

    /**
     * 是否为长连接，默认为true
     * 
     * @return 长连接true，短连接false
     */
    public boolean keepAlive() default true;

}
