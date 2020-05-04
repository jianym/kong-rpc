package org.jeecf.kong.rpc.discover;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端通信上下文
 * 
 * @author jianyiming
 *
 */
public class ContextContainer {

    private static volatile ContextContainer container = null;

    private ContextContainer() {
    }

    public static ContextContainer getInstance() {
        if (container != null)
            return container;
        synchronized (ContextContainer.class) {
            if (container != null)
                return container;
            return container = new ContextContainer();
        }
    }

    private Map<String, ContextEntity> context = new ConcurrentHashMap<>();

    public void put(String key, ContextEntity value) {
        context.put(key, value);
    }
    
    public ContextEntity get(String key) {
        return context.get(key);
    }

    public void remove(String key) {
        context.remove(key);
    }

}
