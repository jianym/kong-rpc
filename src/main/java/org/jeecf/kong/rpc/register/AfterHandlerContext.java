package org.jeecf.kong.rpc.register;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.jeecf.kong.rpc.common.HandlerContext;
import org.jeecf.kong.rpc.common.RequestNode;
import org.jeecf.kong.rpc.protocol.serializer.Request;
import org.jeecf.kong.rpc.protocol.serializer.Response;
import org.jeecf.kong.rpc.register.ProviderContainer.RequestServerNode;

/**
 * 后置拦截上下文
 * 
 * @author jianyiming
 *
 */
public class AfterHandlerContext extends HandlerContext {

    private volatile static AfterHandlerContext context = null;

    private AfterHandlerContext() {
    };

    public static AfterHandlerContext getInstance() {
        if (context != null)
            return context;
        synchronized (AfterHandlerContext.class) {
            if (context != null)
                return context;
            return context = new AfterHandlerContext();
        }
    }

    private List<AfterNode> nodes = new LinkedList<>();

    protected void addNode(AfterNode node) {
        nodes.add(node);
    }

    public void exec(Object[] args, Object result, RequestServerNode reqNode, Request req, Response res) throws Throwable {
        if (CollectionUtils.isNotEmpty(nodes)) {
            AfterJoinPoint joinPoint = new AfterJoinPoint(args, result, reqNode, req, res);
            for (AfterNode node : nodes) {
                boolean isTarget = isTarget(req.getPath(), node.getBasePath());
                if (isTarget) {
                    Method m = node.getM();
                    try {
                        m.invoke(node.getInstance(), joinPoint);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    } catch (Exception e) {
                        throw e;
                    }
                }
            }
        }
    }

    public class AfterJoinPoint extends JoinPoint {

        private Object[] args;

        private Object result;

        public AfterJoinPoint(Object[] args, Object result, RequestNode node, Request req, Response res) {
            super(node, req, res);
            this.args = args;
            this.result = result;
        }

        public Object[] getArgs() {
            return args;
        }

        public Object getResult() {
            return result;
        }
    }

    protected class AfterNode extends Node {
    }

}
