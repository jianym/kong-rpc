package org.jeecf.kong.rpc.discover;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.jeecf.kong.rpc.discover.KrpcClientContainer.RequestClientNode;
import org.jeecf.kong.rpc.protocol.serializer.Request;
import org.jeecf.kong.rpc.protocol.serializer.Response;

/**
 * 客户端后置拦截上下文
 * 
 * @author jianyiming
 *
 */
public class AfterHandlerContext extends ClientHandlerContext {

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

    public void exec(Object result, RequestClientNode reqNode, Request req, Response res) throws Exception {
        if (CollectionUtils.isNotEmpty(nodes)) {
            AfterJoinPoint joinPoint = new AfterJoinPoint(result, reqNode, req, res);
            for (AfterNode node : nodes) {
                boolean isTarget = isTarget(reqNode.getAlias(), node.getAlias(), req.getPath(), node.getBasePath());
                if (isTarget) {
                    Method m = node.getM();
                    try {
                        m.invoke(node.getInstance(), joinPoint);
                    } catch (InvocationTargetException e) {
                        throw (Exception) e.getCause();
                    } catch (Exception e) {
                        throw e;
                    }
                }
            }
        }
    }

    public class AfterJoinPoint extends ClientJoinPoint {

        private Object result;

        public AfterJoinPoint(Object result, RequestClientNode node, Request req, Response res) {
            super(node, req, res);
            this.result = result;
            this.cr = System.currentTimeMillis();
        }

        public Object getResult() {
            return result;
        }
    }

    protected class AfterNode extends ClientNode {
    }

}
