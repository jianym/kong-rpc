package org.jeecf.kong.rpc.exchange;

import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLEngine;
/**
 * ssl 通信引擎
 * 
 * @author jianyiming
 *
 */
public abstract class SslSocketEngine {

    private static Map<String, SSLEngine> sslSocketEngineContainer = new HashMap<>();
    /**
     * 创建对象比初始化引擎
     */
    public void init() {
        this.initSocketEngines(sslSocketEngineContainer);
    }

    public static SSLEngine get(String alias) {
        return SslSocketEngine.sslSocketEngineContainer.get(alias);
    }
    /**
     * map key为服务器连接别名
     * 
     * @param map
     */
    public abstract void initSocketEngines(Map<String, SSLEngine> map);

}
