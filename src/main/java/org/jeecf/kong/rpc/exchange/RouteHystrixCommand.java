package org.jeecf.kong.rpc.exchange;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import org.jeecf.common.lang.StringUtils;
import org.jeecf.common.mapper.JsonMapper;
import org.jeecf.kong.rpc.common.DistributeId;
import org.jeecf.kong.rpc.common.SpringContextUtils;
import org.jeecf.kong.rpc.common.ThreadContainer;
import org.jeecf.kong.rpc.common.exception.KrpcClientException;
import org.jeecf.kong.rpc.discover.AfterHandlerContext;
import org.jeecf.kong.rpc.discover.AroundHandlerContext;
import org.jeecf.kong.rpc.discover.BeforeHandlerContext;
import org.jeecf.kong.rpc.discover.ContextContainer;
import org.jeecf.kong.rpc.discover.ContextEntity;
import org.jeecf.kong.rpc.discover.ExceptionHandlerContext;
import org.jeecf.kong.rpc.discover.ExceptionHandlerContext.ExceptionNode;
import org.jeecf.kong.rpc.discover.KrpcClientContainer.RequestClientNode;
import org.jeecf.kong.rpc.discover.properties.KrpcClientProperties;

import com.netflix.hystrix.HystrixCommand;

/**
 * 依赖Hystrix 路由命令入口
 * 
 * @author jianyiming
 *
 * @param <T>
 */
public class RouteHystrixCommand<T> extends HystrixCommand<T> {

    private static BeforeHandlerContext beforeHandler = BeforeHandlerContext.getInstance();

    private static AfterHandlerContext afterHandlerContext = AfterHandlerContext.getInstance();

    private static AroundHandlerContext aroundHandler = AroundHandlerContext.getInstance();

    private static ExceptionHandlerContext exHandler = ExceptionHandlerContext.getInstance();

    private static KrpcClientProperties properties = SpringContextUtils.getBean(KrpcClientProperties.class);

    private RequestClientNode node;

//    private Request req;

    public RouteHystrixCommand(RequestClientNode node, Setter setter) {
        super(setter);
        this.node = node;
//        this.req = req;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T run() throws Exception {
        ThreadContainer threadContainer = SpringContextUtils.getBean(ThreadContainer.class);
        String span = threadContainer.get(ThreadContainer.SPAN);
        boolean isBoot = true;
        if (threadContainer.get(ThreadContainer.ID) == null) {
            isBoot = false;
            threadContainer.set(ThreadContainer.ID, DistributeId.getId());
        }
        if (span == null) {
            isBoot = false;
            span = DistributeId.getId();
        }
        try {
            if (StringUtils.isEmpty(node.getAlias())) {
                node.setAlias(properties.getName());
            }
            this.node.setClientSpan(span);
            this.node.setTraceId(threadContainer.get(ThreadContainer.ID));
            this.node.setTime(System.currentTimeMillis());
            ContextEntity entity = buildContext();
            Route route = RouteFactory.getRoute(node.getAlias(), this.node.getArgs());
            beforeHandler.exec(node);
            T result = (T) aroundHandler.exec(node, route);
            afterHandlerContext.exec(result, node, entity.getResponseHelper());
            return result;
        } finally {
            if (!isBoot) {
                threadContainer.remove();
            }
            ContextContainer.getInstance().remove(span);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getFallback() {
        Throwable e = getFailedExecutionException();
        ExceptionNode exNode;
        try {
            Object fallback = node.getFallBack();
            if (fallback != null) {
                Method m = node.getMethod();
                Object[] args = null;
                String jsonData = this.node.getArgs();
                if (StringUtils.isNotEmpty(jsonData)) {
                    Map<String, Object> mapArgs = JsonMapper.getInstance().readValue(jsonData, Map.class);
                    Parameter[] ps = m.getParameters();
                    if (ps != null) {
                        args = new Object[ps.length];
                        for (int i = 0; i < ps.length; i++) {
                            Object value = mapArgs.get(ps[i].getName());
                            args[i] = value;
                        }
                    }
                }
                return (T) m.invoke(fallback, args);
            }
            exNode = exHandler.exec(e, node, null);
            if (exNode == null) {
                throw new KrpcClientException(e);
            }
            if (exNode.getResult() != null)
                return (T) exNode.getResult();
            return null;
        } catch (Throwable e1) {
            throw new KrpcClientException(e1);
        }
    }

    private ContextEntity buildContext() {
        ContextEntity entity = new ContextEntity();
        entity.setThread(Thread.currentThread());
        ContextContainer.getInstance().put(this.node.getClientSpan(), entity);
        return entity;
    }

}
