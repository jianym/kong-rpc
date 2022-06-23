package org.jeecf.kong.rpc.register;

import java.lang.reflect.Modifier;

import org.jeecf.common.mapper.JsonMapper;
import org.jeecf.kong.rpc.EnableKrpcRegister;
import org.jeecf.kong.rpc.center.CenterNode;
import org.jeecf.kong.rpc.common.DistributeId;
import org.jeecf.kong.rpc.common.exception.NotExistSslEngineException;
import org.jeecf.kong.rpc.common.exception.NotFoundAliasException;
import org.jeecf.kong.rpc.discover.ConsumerDiscover;
import org.jeecf.kong.rpc.exchange.SslServerSocketEngine;
import org.jeecf.kong.rpc.protocol.NettyServer;
import org.jeecf.kong.rpc.register.annotation.KrpcServer;
import org.jeecf.kong.rpc.register.annotation.KrpcServerAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
    private KrpcServerProperties properties;
    /**
     * 防止重入
     */
    private volatile boolean isEntry;

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (isEntry()) {
            return;
        }
        try {
            String[] beans = event.getApplicationContext().getBeanDefinitionNames();
            SslServerSocketEngine engine = null;
            for (int i = 0; i < beans.length; i++) {
                Object o = event.getApplicationContext().getBean(beans[i]);
                Class<?> clazz = o.getClass();
                if (clazz.getAnnotation(KrpcServer.class) != null) {
                    requestMappingRegister.register(clazz, o);
                }
                if (clazz.getAnnotation(KrpcServerAdvice.class) != null) {
                    serverhandlerRegister.register(clazz, o);
                }
                EnableKrpcRegister register = AnnotationUtils.findAnnotation(clazz, EnableKrpcRegister.class);
                if (register != null) {
                    Class<? extends SslServerSocketEngine> engineCLass = register.sslEngine();
                    if (!Modifier.isAbstract(engineCLass.getModifiers())) {
                        engine = engineCLass.newInstance();
                        engine.init();
                    }
                }
            }

            String ip = DistributeId.getLocalHostLANAddress().getHostAddress();
            Integer port = properties.getPort();
            String name = properties.getName();
            if (StringUtils.isEmpty(name)) {
                throw new NotFoundAliasException("alias no exits...");
            }

            CenterNode zkNode = new CenterNode();
            zkNode.setIp(ip);
            zkNode.setPort(port);
            zkNode.setName(name);
            String path = "/server/" + ip + "-" + port;
            ZkRegister.register(path, JsonMapper.toJson(zkNode), properties.getZookeeper());
            NettyServer server = null;
            if (properties.isSsl()) {
                if (engine == null || engine.get() == null)
                    throw new NotExistSslEngineException("not exist server SSLEngine....");
                server = new NettyServer(properties.getSocket(), engine.get());
            } else
                server = new NettyServer(properties.getSocket(), null);
            server.run(properties.getPort());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            System.exit(0);
        }

    }

    public boolean isEntry() {
        if (isEntry) {
            return isEntry;
        }
        synchronized (ConsumerDiscover.class) {
            if (isEntry) {
                return isEntry;
            }
            isEntry = true;
            return false;
        }
    }

}
