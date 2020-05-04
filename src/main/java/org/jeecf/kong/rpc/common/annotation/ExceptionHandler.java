package org.jeecf.kong.rpc.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 异常拦截
 * 
 * @author jianyiming
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExceptionHandler {

    public Class<? extends Throwable>[] ex() default {Throwable.class};

    public String[] basePath() default {};

}
