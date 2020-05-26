package org.jeecf.kong.rpc.discover;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jeecf.kong.rpc.discover.KrpcClientContainer.RequestClientNode;
import org.jeecf.kong.rpc.exchange.Route;
import org.jeecf.kong.rpc.protocol.serializer.Request;

/**
 * 环绕拦截上下文
 * 
 * @author jianyiming
 *
 */
public class AroundHandlerContext extends ClientHandlerContext {

    private volatile static AroundHandlerContext context = null;

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

    public Object exec(RequestClientNode reqNode, Route route, Request req) throws Exception {
        AroundJoinPoint joinPoint = new AroundJoinPoint(head, route, reqNode, req);
        return joinPoint.processon();
    }

    public class AroundJoinPoint extends ClientJoinPoint {

        private AroundNode node;

        private Object result;

        private Route route;

        private Request request;

        private RequestClientNode reqNode;

        public AroundJoinPoint(AroundNode node, Route route, RequestClientNode reqNode, Request req) {
            super(reqNode, req, null);
            this.node = node;
            this.request = req;
            this.route = route;
            this.reqNode = reqNode;
        }

        public Object processon() throws Exception {
            while (node != null) {
                boolean isTarget = isTarget(getPath(), node.getBasePath());
                if (isTarget) {
                    Method m = node.getM();
                    try {
                        Object instance = node.getInstance();
                        node = node.getNext();
                        return m.invoke(instance, getSuper());
                    } catch (InvocationTargetException e) {
                        throw (Exception) e.getCause();
                    } catch (Exception e) {
                        throw e;
                    }
                }
                node = node.getNext();
            }
            return route.send(reqNode, request);
        }

        public ClientJoinPoint getSuper() {
            return this;
        }

        public Object getResult() {
            return result;
        }

    }

    protected class AroundNode extends ClientNode {

        private AroundNode next;

        public AroundNode getNext() {
            return next;
        }

        public void setNext(AroundNode next) {
            this.next = next;
        }

    }

}
