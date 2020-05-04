package org.jeecf.kong.rpc.register;

import org.jeecf.kong.rpc.protocol.NettyServer;
import org.jeecf.kong.rpc.register.annotation.KrpcServer;
import org.jeecf.kong.rpc.register.annotation.KrpcServerAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 服务注册入口
 * 
 * @author jianyiming
 *
 */
@Slf4j
@Component
@ComponentScan(basePackages = { "org.jeecf.kong.rpc.register", "org.jeecf.kong.rpc.common" })
public class ProviderRegister implements ApplicationListener<ContextRefreshedEvent>, Ordered {

    @Autowired
    private RequestMappingRegister requestMappingRegister;

    @Autowired
    private ServerHandlerRegister serverhandlerRegister;

    @Autowired
    private ZkRegister zkRegister;

    @Autowired
    private KrpcServerProperties properties;

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            String[] beans = event.getApplicationContext().getBeanDefinitionNames();
            for (int i = 0; i < beans.length; i++) {
                Object o = event.getApplicationContext().getBean(beans[i]);
                Class<?> clazz = o.getClass();
                if (clazz.getAnnotation(KrpcServer.class) != null) {
                    requestMappingRegister.register(clazz, o);
                }
                if (clazz.getAnnotation(KrpcServerAdvice.class) != null) {
                    serverhandlerRegister.register(clazz, o);
                }
            }
            zkRegister.register();
            NettyServer server = new NettyServer(properties.getSocket());
            server.run(properties.getPort());
        } catch (Exception e) {
           log.error(e.getMessage(),e);
           System.exit(0);
        }

    }

}
