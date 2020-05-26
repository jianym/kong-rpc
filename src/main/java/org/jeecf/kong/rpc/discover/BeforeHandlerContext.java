package org.jeecf.kong.rpc.discover;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.jeecf.kong.rpc.discover.KrpcClientContainer.RequestClientNode;
import org.jeecf.kong.rpc.protocol.serializer.Request;

/**
 * 客户端前置拦截上下文
 * 
 * @author jianyiming
 *
 */
public class BeforeHandlerContext extends ClientHandlerContext {

    private volatile static BeforeHandlerContext context = null;

    private BeforeHandlerContext() {
    };

    public static BeforeHandlerContext getInstance() {
        if (context != null)
            return context;
        synchronized (BeforeHandlerContext.class) {
            if (context != null)
                return context;
            return context = new BeforeHandlerContext();
        }
    }

    private List<BeforeNode> nodes = new LinkedList<>();

    protected void addNode(BeforeNode node) {
        nodes.add(node);
    }

    public void exec(RequestClientNode reqNode, Request req) throws Exception {
        if (CollectionUtils.isNotEmpty(nodes)) {
            BeforeJoinPoint joinPoint = new BeforeJoinPoint(reqNode, req);
            for (BeforeNode node : nodes) {
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

    public class BeforeJoinPoint extends ClientJoinPoint {

        public BeforeJoinPoint(RequestClientNode reqNode, Request req) {
            super(reqNode, req, null);
        }

    }

    protected class BeforeNode extends ClientNode {
    }

}
