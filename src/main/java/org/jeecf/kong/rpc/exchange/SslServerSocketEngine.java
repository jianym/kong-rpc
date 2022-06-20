package org.jeecf.kong.rpc.exchange;

import javax.net.ssl.SSLEngine;

/**
 * ssl 服务端引擎
 * 
 * @author jianyiming
 *
 */
public abstract class SslServerSocketEngine {

    private static SSLEngine serverSocketEngine = null;

    /**
     * 创建对象比初始化引擎
     */
    public void init() {
        SslServerSocketEngine.serverSocketEngine = initServerSocketEngines();
    }

    public abstract SSLEngine initServerSocketEngines();

    public static SSLEngine get() {
        return serverSocketEngine;
    }

}
