package org.jeecf.kong.rpc.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * springbean 加载工具
 * 
 * @author jianyiming
 *
 */
@Component("springKrpcContextUtils")
public class SpringContextUtils implements ApplicationContextAware {

    private static ApplicationContext APPLICATION_CONTEXT;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringContextUtils.APPLICATION_CONTEXT == null) {
            SpringContextUtils.APPLICATION_CONTEXT = applicationContext;
        }
    }

    /**
     * 获取applicationContext
     * 
     * @return bean容器上下文
     */
    public static ApplicationContext getApplicationContext() {
        return APPLICATION_CONTEXT;
    }

    /**
     * 通过name获取 Bean.
     * 
     * @param name
     * @return bean实体
     */
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    /**
     * 通过class获取Bean.
     * 
     * @param clazz
     * @return bean实体
     */
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     * 
     * @param name
     * @param clazz
     * @return bean实体
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

}
