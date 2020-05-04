package org.jeecf.kong.rpc.protocol.properties;

/**
 * 服务端通信配置
 * 
 * @author jianyiming
 *
 */
public class ServerSocketProperties {

    private int low = 32 * 1024;

    private int height = 64 * 1024;

    private int back = 50;

    private int timeout = 0;

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

    public int getBack() {
        return back;
    }

    public void setBack(int back) {
        this.back = back;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

}