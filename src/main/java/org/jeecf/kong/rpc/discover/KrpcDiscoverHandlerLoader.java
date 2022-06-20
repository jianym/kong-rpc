package org.jeecf.kong.rpc.discover;

import java.lang.reflect.Modifier;

import org.jeecf.kong.rpc.EnableKrpcDiscover;
import org.jeecf.kong.rpc.exchange.RetryManager;
import org.jeecf.kong.rpc.exchange.SslSocketEngine;
import org.springframework.stereotype.Component;

/**
 * 加载@EnableKrpcDiscover注解
 * 
 * @author jianyiming
 *
 */
@Component
public class KrpcDiscoverHandlerLoader {

    private ConsumerContainer container = ConsumerContainer.getInstance();

    public void load(Class<?> clazz) throws InstantiationException, IllegalAccessException {
        EnableKrpcDiscover discover = clazz.getAnnotation(EnableKrpcDiscover.class);
        RetryManager retryManager = discover.retryManager().newInstance();
        Class<SslSocketEngine> engineCLass = (Class<SslSocketEngine>) discover.sslEngine();
        if (!Modifier.isAbstract(engineCLass.getModifiers())) {
            SslSocketEngine engine = engineCLass.newInstance();
            engine.init();
            container.setSslEngine(engine);
        }
        container.setRetryManager(retryManager);
    }

}
