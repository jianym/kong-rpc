package org.jeecf.kong.rpc.discover;

import org.jeecf.kong.rpc.common.HandlerContext;
import org.jeecf.kong.rpc.discover.KrpcClientContainer.RequestClientNode;
import org.jeecf.kong.rpc.protocol.serializer.Request;
import org.jeecf.kong.rpc.protocol.serializer.Response;

/**
 * 客户端拦截器上下文
 * 
 * @author jianyiming
 *
 */
public class ClientHandlerContext extends HandlerContext {

    public boolean isTarget(String alias, String[] aliasArray, String path, String[] basePaths) {
        boolean isTarget = isTarget(path, basePaths);
        if (isTarget) {
            if (aliasArray == null || aliasArray.length == 0) {
                return true;
            }
            for (String a : aliasArray) {
                if (alias.equals(a)) {
                    return true;
                }
            }
        }
        return isTarget;
    }

    protected class ClientJoinPoint extends JoinPoint {

        public ClientJoinPoint(RequestClientNode node, Request req, Response res) {
            super(node, req, res);
            this.alias = node.getAlias();
            this.retry = node.getRetry();
            this.timeout = node.getTimeout();
            this.args = req.getArgs();
        }

        /**
         * 参数
         */
        protected String args;
        /**
         * 别名
         */
        protected String alias;
        /**
         * 重试
         */
        protected Integer retry;
        /**
         * 超时
         */
        protected Integer timeout;
        /**
         * 返回类型
         */
        protected Class<?> returnType;

        public String getAlias() {
            return alias;
        }

        public Integer getRetry() {
            return retry;
        }

        public Integer getTimeout() {
            return timeout;
        }

        public String getArgs() {
            return args;
        }

        public Class<?> getReturnType() {
            return returnType;
        }

        public void setReturnType(Class<?> returnType) {
            this.returnType = returnType;
        }

    }

    protected class ClientNode extends Node {
        /**
         * 拦截别名
         */
        private String[] alias;

        public String[] getAlias() {
            return alias;
        }

        public void setAlias(String[] alias) {
            this.alias = alias;
        }

    }

}
