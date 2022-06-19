package org.jeecf.kong.rpc.register;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jeecf.kong.rpc.common.RequestNode;
import org.jeecf.kong.rpc.protocol.serializer.ConstantValue;

/**
 * 服务端容器
 * 
 * @author jianyiming
 *
 */
public class ProviderContainer {

    private volatile static ProviderContainer providerContainer = null;

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

    private Map<String, RequestServerNode> requestNodeShardMap = new ConcurrentHashMap<>();

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

    public void add(String key, RequestServerNode node, byte transferMode) {
        if (transferMode == ConstantValue.WHOLE_MODE)
            requestNodeMap.put(key, node);
        else
            requestNodeShardMap.put(key, node);
    }

    public Map<String, RequestServerNode> getMap(byte transferMode) {
        if (transferMode == ConstantValue.WHOLE_MODE)
            return requestNodeMap;
        else
            return requestNodeShardMap;
    }

    public Set<String> getKeys(byte transferMode) {
        if (transferMode == ConstantValue.WHOLE_MODE)
            return requestNodeMap.keySet();
        else
            return requestNodeShardMap.keySet();
    }

    public RequestServerNode get(String key, byte transferMode) {
        if (transferMode == ConstantValue.WHOLE_MODE)
            return requestNodeMap.get(key);
        else
            return requestNodeShardMap.get(key);
    }

    public RequestServerNode removeShard(String key) {
        return requestNodeShardMap.remove(key);
    }

    public class RequestServerNode extends RequestNode {
    }

}
