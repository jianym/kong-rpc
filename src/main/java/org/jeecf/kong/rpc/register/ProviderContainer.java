package org.jeecf.kong.rpc.register;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jeecf.kong.rpc.common.RequestNode;

/**
 * 服务端容器
 * 
 * @author jianyiming
 *
 */
public class ProviderContainer {

    private static ProviderContainer providerContainer = null;

    private ProviderContainer() {
    };

    public static ProviderContainer getInstance() {
        if (providerContainer != null)
            return providerContainer;
        synchronized (ProviderContainer.class) {
            if (providerContainer != null)
                return providerContainer;
            return providerContainer = new ProviderContainer();
        }
    }

    private Map<String, RequestServerNode> requestNodeMap = new ConcurrentHashMap<>();

    public void add(String path, RequestServerNode node) {
        requestNodeMap.put(path, node);
    }

    public Map<String, RequestServerNode> getMap() {
        return requestNodeMap;
    }

    public Set<String> getKeys() {
        return requestNodeMap.keySet();
    }

    public RequestServerNode get(String path) {
        return requestNodeMap.get(path);
    }

    public class RequestServerNode extends RequestNode {
    }

}
