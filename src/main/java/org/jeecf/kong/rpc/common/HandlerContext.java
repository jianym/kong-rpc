package org.jeecf.kong.rpc.common;

import java.lang.reflect.Method;

import org.jeecf.kong.rpc.protocol.serializer.Response;

/**
 * 拦截器上下文
 * 
 * @author jianyiming
 *
 */
public class HandlerContext {

    protected boolean isTarget(String path, String[] basePaths) {
        if (basePaths == null || basePaths.length == 0) {
            return true;
        }
        for (String basePath : basePaths) {
            if (path.startsWith(basePath)) {
                return true;
            }
        }
        return false;
    }

    protected class JoinPoint {
        /**
         * 全局id
         */
        protected String traceId;
        /**
         * 客户端span
         */
        protected String clientSpan;
        /**
         * 服务端span
         */
        protected String serverSpan;
        /**
         * 客户端发送时间
         */
        protected Long cs = 0l;
        /**
         * 服务端接收时间
         */
        protected Long sr = 0l;
        /**
         * 服务端发送时间
         */
        protected Long ss = 0l;
        /**
         * 客户端接收时间
         */
        protected Long cr = 0l;
        /**
         * 目标对象
         */
        protected Object target;
        /**
         * 目标方法
         */
        protected Method targetMethod;
        /**
         * 路径
         */
        protected String path;
        /**
         * 版本
         */
        protected Integer version = 0;

        public JoinPoint(RequestNode node,  Response res) {
            if (node != null) {
                this.targetMethod = node.getMethod();
                this.target = node.getInstance();
            }
            this.path = node.getPath();
            this.clientSpan = node.getClientSpan();
            this.traceId = node.getTraceId();
            this.version = node.getVersion();
            this.cs = node.getTime();
            if (res != null) {
                this.ss = res.getSs();
                this.sr = res.getSr();
                this.serverSpan = res.getServerSpan();
            }
        }

        public String getTraceId() {
            return traceId;
        }

        public Long getCs() {
            return cs;
        }

        public Object getTarget() {
            return target;
        }

        public Method getTargetMethod() {
            return targetMethod;
        }

        public String getPath() {
            return path;
        }

        public Integer getVersion() {
            return version;
        }

        public String getClientSpan() {
            return clientSpan;
        }

        public String getServerSpan() {
            return serverSpan;
        }

        public Long getSr() {
            return sr;
        }

        public Long getSs() {
            return ss;
        }

        public Long getCr() {
            return cr;
        }

    }

    protected class Node {
        /**
         * 拦截对象
         */
        protected Object instance;
        /**
         * 拦截方法
         */
        protected Method m;
        /**
         * 拦截路径
         */
        protected String[] basePath;

        public Object getInstance() {
            return instance;
        }

        public void setInstance(Object instance) {
            this.instance = instance;
        }

        public Method getM() {
            return m;
        }

        public void setM(Method m) {
            this.m = m;
        }

        public String[] getBasePath() {
            return basePath;
        }

        public void setBasePath(String[] basePath) {
            this.basePath = basePath;
        }
    }

}
