package org.jeecf.kong.rpc.discover;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.jeecf.kong.rpc.discover.KrpcClientContainer.RequestClientNode;
import org.jeecf.kong.rpc.protocol.serializer.Request;
import org.jeecf.kong.rpc.protocol.serializer.Response;

/**
 * 客户端异常拦截上下文
 * 
 * @author jianyiming
 *
 */
public class ExceptionHandlerContext extends ClientHandlerContext {

    private volatile static ExceptionHandlerContext context = null;

    private ExceptionHandlerContext() {
    };

    public static ExceptionHandlerContext getInstance() {
        if (context != null)
            return context;
        synchronized (ExceptionHandlerContext.class) {
            if (context != null)
                return context;
            return context = new ExceptionHandlerContext();
        }
    }

    private Map<Class<? extends Throwable>, ExceptionNode> tMap = new TreeMap<>(new Comparator<Class<? extends Throwable>>() {
        @Override
        public int compare(Class<? extends Throwable> o1, Class<? extends Throwable> o2) {
            if (o1 == o2) {
                return 0;
            }
            if (o1.isAssignableFrom(o2))
                return 1;
            return -1;
        }
    });

    protected void addNode(ExceptionNode node) {
        Class<? extends Throwable>[] clazzs = node.getEx();
        for (Class<? extends Throwable> clazz : clazzs) {
            tMap.putIfAbsent(clazz, node);
        }
    }

    public ExceptionNode exec(Throwable ex, RequestClientNode reqNode, Request req, Response res) throws Throwable {
        if (tMap == null || tMap.size() == 0) {
            return null;
        }
        ExceptionJoinPoint joinPoint = new ExceptionJoinPoint(ex, reqNode, req, res);
        for (Map.Entry<Class<? extends Throwable>, ExceptionNode> entry : tMap.entrySet()) {
            Class<? extends Throwable> key = entry.getKey();
            if (key == ex.getClass() || key.isAssignableFrom(ex.getClass())) {
                ExceptionNode node = entry.getValue();
                boolean isTarget = isTarget(reqNode.getAlias(), node.getAlias(), req.getPath(), node.getBasePath());
                if (isTarget) {
                    Method m = node.getM();
                    try {
                        Object eR = m.invoke(node.getInstance(), joinPoint);
                        node.setResult(eR);
                        return node;
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    } catch (Exception e) {
                        throw e;
                    }
                }
            }
        }
        return null;
    }

    public class ExceptionJoinPoint extends ClientJoinPoint {

        private Throwable ex;

        public ExceptionJoinPoint(Throwable ex, RequestClientNode reqNode, Request req, Response res) {
            super(reqNode, req, res);
            this.ex = ex;
        }

        public Throwable getEx() {
            return ex;
        }

    }

    public class ExceptionNode extends ClientNode {

        private Class<? extends Throwable>[] ex;

        private Object result;

        public Class<? extends Throwable>[] getEx() {
            return ex;
        }

        public void setEx(Class<? extends Throwable>[] ex) {
            this.ex = ex;
        }

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
        }

    }

}
