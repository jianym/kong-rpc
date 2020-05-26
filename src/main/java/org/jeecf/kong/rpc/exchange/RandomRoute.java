package org.jeecf.kong.rpc.exchange;

import java.util.Random;

import org.jeecf.kong.rpc.discover.ConsumerContainer.ServerNode;

/**
 * 随机路由
 * 
 * @author jianyiming
 *
 */
public class RandomRoute extends Route {

    private volatile static RandomRoute route = null;

    Random random = new Random();

    private RandomRoute() {
    }

    public static Route getInstance() {
        if (route != null) {
            return route;
        }
        synchronized (Route.class) {
            if (route != null) {
                return route;
            }
            return route = new RandomRoute();
        }
    }

    @Override
    protected ServerNode getServerNode(String alias, String data) {
        ServerNode node = null;
        int size = consumerContainer.size(alias);
        if (size > 0) {
            node = consumerContainer.getIndex(alias, random.nextInt(size));
        }
        NettyClientFactory.getInstance(node);
        return node;
    }

}
