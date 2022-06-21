package org.jeecf.kong.rpc.discover;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jeecf.kong.rpc.common.RequestNode;
import org.jeecf.kong.rpc.protocol.serializer.ConstantValue;

/**
 * krpclient 容器 用于@krpcClient注解
 * 
 * @author jianyiming
 *
 */
public class KrpcClientContainer {

    private volatile static KrpcClientContainer container;

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

    private Map<String, RequestClientNode> proxyMap = new ConcurrentHashMap<>();

    public void put(String key, RequestClientNode requestNode) {
        proxyMap.put(key, requestNode);
    }

    public RequestClientNode get(String key) {
        return proxyMap.get(key);
    }

    public class RequestClientNode extends RequestNode {

        private String alias;

        private int retry;

        private int timeout;

        private boolean sync;

        private Class<?> returnType;

        private Object fallBack;

        private boolean keepAlive;

        private String clientId;

        private byte transferMode = ConstantValue.WHOLE_MODE;

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

        public Object getFallBack() {
            return fallBack;
        }

        public void setFallBack(Object fallBack) {
            this.fallBack = fallBack;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public byte getTransferMode() {
            return transferMode;
        }

        public void setTransferMode(byte transferMode) {
            this.transferMode = transferMode;
        }

        public boolean isKeepAlive() {
            return keepAlive;
        }

        public void setKeepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
        }

    }

}
