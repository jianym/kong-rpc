package org.jeecf.kong.rpc.common;

import java.lang.reflect.Method;

/**
 * 请求节点
 * 
 * @author jianyiming
 *
 */
public class RequestNode {
    /**
     * 路径
     */
    private String path;
    /**
     * 对象
     */
    private Object instance;
    /**
     * 方法
     */
    private Method method;
    /**
     * 版本
     */
    private int version;

    public RequestNode() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

}
