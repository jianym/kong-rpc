package org.jeecf.kong.rpc.discover;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.jeecf.kong.rpc.EnableKrpcDiscover;
import org.jeecf.kong.rpc.discover.annotation.KrpcAutowired;
import org.jeecf.kong.rpc.discover.annotation.KrpcClient;
import org.jeecf.kong.rpc.discover.annotation.KrpcClientAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 服务发现
 * 
 * @author jianyiming
 *
 */
@Slf4j
@Component
@ComponentScan(basePackages = { "org.jeecf.kong.rpc.discover", "org.jeecf.kong.rpc.common" })
public class ConsumerDiscover implements ApplicationListener<ContextRefreshedEvent>, Ordered {

    @Autowired
    private ZkServerListener zkListener;

    @Autowired
    private KrpcClientLoader krpcClientLoader;

    @Autowired
    private ClientHandlerLoader clientHandlerLoader;

    @Autowired
    private KrpcDiscoverHandlerLoader krpcDiscoverHandlerLoader;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            String[] beans = event.getApplicationContext().getBeanDefinitionNames();
            Map<Class<?>, Object> fieldMap = new HashMap<>();
            for (int i = 0; i < beans.length; i++) {
                Object o = event.getApplicationContext().getBean(beans[i]);
                Class<?> clazz = o.getClass();
                Field[] fields = clazz.getDeclaredFields();
                if (fields != null && fields.length > 0) {
                    for (Field field : fields) {
                        if (field.getAnnotation(KrpcAutowired.class) != null) {
                            Class<?> filedClass = field.getType();
                            KrpcClient client = filedClass.getAnnotation(KrpcClient.class);
                            if (filedClass.isInterface() && client != null) {
                                Object proxyBean = null;
                                if (fieldMap.get(filedClass) != null) {
                                    proxyBean = fieldMap.get(filedClass);
                                } else {
                                    proxyBean = krpcClientLoader.load(filedClass, o, client);
                                    fieldMap.put(filedClass, proxyBean);
                                }
                                try {
                                    field.setAccessible(true);
                                    field.set(o, proxyBean);
                                } catch (IllegalArgumentException | IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                throw new RuntimeException("@KrpcAutowired " + filedClass.getName() + " not exist @KrpcClient or is not interface ");
                            }
                        }
                    }
                }
                if (clazz.getAnnotation(KrpcClientAdvice.class) != null) {
                    clientHandlerLoader.load(clazz, o);
                }
                EnableKrpcDiscover discover = AnnotationUtils.findAnnotation(clazz, EnableKrpcDiscover.class);
                if (discover != null) {
                    krpcDiscoverHandlerLoader.load(discover);
                }

            }

            zkListener.load();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            System.exit(0);
        }
    }

    @Override
    public int getOrder() {
        // TODO Auto-generated method stub
        return 0;
    }

}
