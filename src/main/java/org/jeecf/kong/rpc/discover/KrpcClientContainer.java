package org.jeecf.kong.rpc.discover;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jeecf.kong.rpc.common.RequestNode;

/**
 * krpclient 容器 用于@krpcClient注解
 * 
 * @author jianyiming
 *
 */
public class KrpcClientContainer {

    private static KrpcClientContainer container;

    private KrpcClientContainer() {
    }

    public static KrpcClientContainer getInstance() {
        if (container != null)
            return container;
        synchronized (KrpcClientContainer.class) {
            if (container != null)
                return container;
            return container = new KrpcClientContainer();
        }
    }

    private Map<Class<?>, RequestClientNode> proxyMap = new ConcurrentHashMap<>();

    public void put(Class<?> proxyBean, RequestClientNode requestNode) {
        proxyMap.put(proxyBean, requestNode);
    }

    public RequestClientNode get(Class<?> proxyBean) {
        return proxyMap.get(proxyBean);
    }

    public class RequestClientNode extends RequestNode {

        private String alias;

        private int retry;

        private int timeout;

        private boolean sync;

        private Class<?> returnType;

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public int getRetry() {
            return retry;
        }

        public void setRetry(int retry) {
            this.retry = retry;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public Class<?> getReturnType() {
            return returnType;
        }

        public void setReturnType(Class<?> returnType) {
            this.returnType = returnType;
        }

        public boolean isSync() {
            return sync;
        }

        public void setSync(boolean sync) {
            this.sync = sync;
        }

    }

}
