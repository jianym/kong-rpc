package org.jeecf.kong.rpc.common;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * 线程容器
 * 
 * @author jianyiming
 *
 */
@Component
public class ThreadContainer {

    public static final String ID = "KRPC_ID";
    public static final String SPAN = "KRPC_SPAN";

    private ThreadLocal<Map<String, String>> local = new InheritableThreadLocal<>();

    /**
     * 设置参数
     * 
     * @param key
     * @param value
     */
    public void set(String key, String value) {
        Map<String, String> localMap = local.get();
        if (localMap == null) {
            localMap = new HashMap<String, String>(10);
            local.set(localMap);
        }
        localMap.put(key, value);
    }

    /**
     * 获取参数
     * 
     * @param key
     * @return 当前线程副本值
     */
    public String get(String key) {
        Map<String, String> localMap = local.get();
        if (localMap != null) {
            return localMap.get(key);
        }
        return null;
    }

    /**
     * 回收
     */
    public void remove() {
        local.remove();
    }

}
