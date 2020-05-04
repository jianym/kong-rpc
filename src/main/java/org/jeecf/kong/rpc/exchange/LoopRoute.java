package org.jeecf.kong.rpc.exchange;

import java.util.concurrent.atomic.AtomicInteger;

import org.jeecf.kong.rpc.discover.ConsumerContainer.ServerNode;

/**
 * 轮询路由
 * 
 * @author jianyiming
 *
 */
public class LoopRoute extends Route {

    private static LoopRoute route = null;

    AtomicInteger atomicInt = new AtomicInteger(0);

    private LoopRoute() {
    }

    public static Route getInstance() {
        if (route != null) {
            return route;
        }
        synchronized (Route.class) {
            if (route != null) {
                return route;
            }
            return route = new LoopRoute();
        }
    }

    @Override
    protected ServerNode getServerNode(String alias, String data) {
        ServerNode node = null;
        atomicInt.compareAndSet(Integer.MAX_VALUE - 1, 0);
        int index = atomicInt.getAndIncrement();
        int size = consumerContainer.size(alias);
        if (size > 0) {
            node = consumerContainer.getIndex(alias, index & (size - 1));
        }
        NettyClientFactory.getInstance(node);
        return node;
    };

}
