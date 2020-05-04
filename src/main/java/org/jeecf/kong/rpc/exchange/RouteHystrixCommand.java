package org.jeecf.kong.rpc.exchange;

import org.jeecf.common.lang.StringUtils;
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
import org.jeecf.kong.rpc.protocol.serializer.Request;

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

    private RequestClientNode node;

    private Request req;

    public RouteHystrixCommand(RequestClientNode node, Request req, Setter setter) {
        super(setter);
        this.node = node;
        this.req = req;
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
            if(StringUtils.isEmpty(node.getAlias())) {
                node.setAlias("");
            }
            req.setClientSpan(span);
            req.setId(threadContainer.get(ThreadContainer.ID));
            req.setTime(System.currentTimeMillis());
            ContextEntity entity = buildContext();
            Route route = RouteFactory.getRoute(node.getAlias(), req.getArgs());
            beforeHandler.exec(node, req);
            T result = (T) aroundHandler.exec(node, route, req);
            afterHandlerContext.exec(result, node, req, entity.getResponse());
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
            exNode = exHandler.exec(e, node, req, null);
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
        ContextContainer.getInstance().put(req.getClientSpan(), entity);
        return entity;
    }

}
