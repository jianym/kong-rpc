package org.jeecf.kong.rpc.register;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jeecf.kong.rpc.common.HandlerContext;
import org.jeecf.kong.rpc.common.RequestNode;
import org.jeecf.kong.rpc.protocol.serializer.Request;
import org.jeecf.kong.rpc.protocol.serializer.Response;
import org.jeecf.kong.rpc.register.ProviderContainer.RequestServerNode;

/**
 * 环绕拦截上下文
 * 
 * @author jianyiming
 *
 */
public class AroundHandlerContext extends HandlerContext {

    private static AroundHandlerContext context = null;

    private AroundHandlerContext() {
    };

    public static AroundHandlerContext getInstance() {
        if (context != null)
            return context;
        synchronized (AroundHandlerContext.class) {
            if (context != null)
                return context;
            return context = new AroundHandlerContext();
        }
    }

    private AroundNode head = null;

    private AroundNode tail = null;

    protected void addNode(AroundNode node) {
        if (head == null) {
            head = node;
            tail = node;
        } else {
            tail.setNext(node);
            tail = node;
        }
    }

    public Object exec(Object[] args, RequestServerNode reqNode, Request req, Response res) throws Throwable {
        AroundJoinPoint joinPoint = new AroundJoinPoint(args, head, reqNode, req, res);
        return joinPoint.processon();
    }

    public class AroundJoinPoint extends JoinPoint {

        private AroundNode node;

        private Object[] args;

        private Object result;

        public AroundJoinPoint(Object[] args, AroundNode node, RequestNode reqNode, Request req, Response res) {
            super(reqNode, req, res);
            this.args = args;
            this.node = node;
        }

        public Object processon() throws Throwable {
            while (node != null) {
                boolean isTarget = isTarget(getPath(), node.getBasePath());
                if (isTarget) {
                    Method m = node.getM();
                    try {
                        Object instance = node.getInstance();
                        node = node.getNext();
                        return m.invoke(instance, this);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    } catch (Exception e) {
                        throw e;
                    }
                }
                node = node.getNext();
            }
            try {
                return result = targetMethod.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            } catch (Exception e) {
                throw e;
            }
        }

        public Object[] getArgs() {
            return args;
        }

        public Object getResult() {
            return result;
        }

    }

    protected class AroundNode extends Node {

        private AroundNode next;

        public AroundNode getNext() {
            return next;
        }

        public void setNext(AroundNode next) {
            this.next = next;
        }

    }

}
