package org.jeecf.kong.rpc.protocol.properties;

/**
 * 客户端通信配置
 * 
 * @author jianyiming
 *
 */
public class SocketProperties {
    /**
     * 低水位 写入
     */
    private int low = 32 * 1024;
    /**
     * 高水位 限制
     */
    private int height = 64 * 1024;
    /**
     * 通信写空闲超时
     */
    private int timeout = 0;
    /**
     * 初始化激活连接
     */
    private boolean connect = false;
    /**
     * 字节长度，超过当前字节长度则单独创建Netty连接
     */
    private int bytes = 0;

    public int getLow() {
        return low;
    }

    public void setLow(int low) {
        this.low = low;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isConnect() {
        return connect;
    }

    public void setConnect(boolean connect) {
        this.connect = connect;
    }

    public int getBytes() {
        return bytes;
    }

    public void setBytes(int bytes) {
        this.bytes = bytes;
    }

}