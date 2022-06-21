package org.jeecf.kong.rpc.discover.properties;

import org.jeecf.kong.rpc.center.ZkProperties;
import org.jeecf.kong.rpc.protocol.properties.SocketProperties;

/**
 * krpc 别名配置文件
 * 
 * @author jianyiming
 *
 */
public class KrpcProperties {
    /**
     * 别名
     */
    private String name;
    /**
     * 路由
     */
    private String route = "loop";
    /**
     * 开启 ssl验证
     */
    private boolean ssl = false;
    /**
     * 通信
     */
    private SocketProperties socket = new SocketProperties();
    /**
     * zk配置
     */
    private ZkProperties zookeeper = new ZkProperties();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public ZkProperties getZookeeper() {
        return zookeeper;
    }

    public void setZookeeper(ZkProperties zookeeper) {
        this.zookeeper = zookeeper;
    }

    public SocketProperties getSocket() {
        return socket;
    }

    public void setSocket(SocketProperties socket) {
        this.socket = socket;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

}