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
     * 当前全局id
     */
    private String traceId;
    /**
     * 当前
     */
    private String clientSpan;
    /**
     * 请求时间
     */
    private Long time;
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

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getClientSpan() {
        return clientSpan;
    }

    public void setClientSpan(String clientSpan) {
        this.clientSpan = clientSpan;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

}
