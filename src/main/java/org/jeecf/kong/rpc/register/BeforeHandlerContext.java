package org.jeecf.kong.rpc.register;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.jeecf.kong.rpc.common.HandlerContext;
import org.jeecf.kong.rpc.common.RequestNode;
import org.jeecf.kong.rpc.protocol.serializer.Response;
import org.jeecf.kong.rpc.register.ProviderContainer.RequestServerNode;

/**
 * 前置拦截上下文
 * 
 * @author jianyiming
 *
 */
public class BeforeHandlerContext extends HandlerContext {

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

    public void exec(Object[] args, RequestServerNode reqNode, Response res) throws Throwable {
        if (CollectionUtils.isNotEmpty(nodes)) {
            BeforeJoinPoint joinPoint = new BeforeJoinPoint(args, reqNode, res);
            for (BeforeNode node : nodes) {
                boolean isTarget = isTarget(reqNode.getPath(), node.getBasePath());
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

    public class BeforeJoinPoint extends JoinPoint {

        private Object[] args;

        public BeforeJoinPoint(Object[] args, RequestNode reqNode, Response res) {
            super(reqNode, res);
            this.args = args;
        }

        public Object[] getArgs() {
            return args;
        }

    }

    protected class BeforeNode extends Node {
    }

}
