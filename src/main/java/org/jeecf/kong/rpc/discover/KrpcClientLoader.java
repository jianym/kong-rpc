package org.jeecf.kong.rpc.discover;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.jeecf.kong.rpc.common.ResourceLocationUtils;
import org.jeecf.kong.rpc.common.SpringContextUtils;
import org.jeecf.kong.rpc.discover.KrpcClientContainer.RequestClientNode;
import org.jeecf.kong.rpc.discover.annotation.Krpc;
import org.jeecf.kong.rpc.discover.annotation.KrpcClient;
import org.springframework.stereotype.Component;

import net.sf.cglib.proxy.Enhancer;

/**
 * 加载@KrpcClient注解
 * 
 * @author jianyiming
 *
 */
@Component
public class KrpcClientLoader {

    private KrpcClientContainer container = KrpcClientContainer.getInstance();
    
    public Object load(Class<?> clazz, Object bean, KrpcClient client) throws ClassNotFoundException {
        Method[] methods = clazz.getMethods();
        if (methods == null || methods.length == 0) {
            return null;
        }
        int version = client.version();
        int retry = client.retry();
        int timeout = client.timeout();
        Object fallback = null;
        if (StringUtils.isNotEmpty(client.fallback())) {
            Class<?> fallbackClass = Class.forName(client.fallback());
            if (fallbackClass != null && clazz.isAssignableFrom(fallbackClass)) {
                fallback = SpringContextUtils.getBean(fallbackClass);
            } else {
                throw new ClassNotFoundException();
            }
        }
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new ClientProxyInterceptor());
        Object proxyBean = enhancer.create();
        for (Method m : methods) {
            Krpc krpc = m.getAnnotation(Krpc.class);
            if (krpc != null) {
                RequestClientNode node = container.new RequestClientNode();
                String path = ResourceLocationUtils.buildPath(client.value(), krpc.value());
                if (krpc.version() != 0)
                    version = krpc.version();
                node.setVersion(version);
                if (krpc.retry() != 2)
                    retry = krpc.retry();
                node.setVersion(retry);
                if (krpc.timeout() != 3000)
                    timeout = krpc.timeout();
                if (krpc.fallback() && fallback != null) {
                    node.setFallBack(fallback);
                } else {
                    throw new ClassNotFoundException();
                }
                node.setAlias(client.alias());
                node.setPath(path);
                node.setVersion(version);
                node.setRetry(retry);
                node.setInstance(bean);
                node.setMethod(m);
                node.setTimeout(timeout);
                node.setSync(krpc.sync());
                node.setReturnType(m.getReturnType());
                container.put(proxyBean.getClass(), node);
            }
        }
        return proxyBean;
    }

}
