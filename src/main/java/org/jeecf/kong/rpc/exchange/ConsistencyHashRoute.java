package org.jeecf.kong.rpc.exchange;

import org.jeecf.kong.rpc.discover.ConsumerContainer.ServerNode;

/**
 * hash 路由
 * 
 * @author jianyiming
 *
 */
public class ConsistencyHashRoute extends Route {

    private static ConsistencyHashRoute route = null;

    private static final int SIZE_16 = 1 << 16;

    private static final int SIZE_8 = 1 << 8;

    private static final int SIZE_4 = 1 << 4;

    private ConsistencyHashRoute() {
    }

    public static Route getInstance() {
        if (route != null) {
            return route;
        }
        synchronized (Route.class) {
            if (route != null) {
                return route;
            }
            return route = new ConsistencyHashRoute();
        }
    }

    @Override
    protected ServerNode getServerNode(String alias, String data) {
        int hashcode = data.hashCode();
        ServerNode node = null;

        int size = consumerContainer.size(alias);
        if (size > 0) {
            node = consumerContainer.getIndex(alias, getHash(size, hashcode) & (size - 1));
        }
        NettyClientFactory.getInstance(node);
        return node;
    }

    private int getHash(int size, int hashcode) {
        if (size < SIZE_4) {
            hashcode = hashcode ^ (hashcode >>> 16);
            hashcode = hashcode ^ (hashcode >>> 8);
            hashcode = hashcode ^ (hashcode >>> 4);
        } else if (size < SIZE_8) {
            hashcode = hashcode ^ (hashcode >>> 16);
            hashcode = hashcode ^ (hashcode >>> 8);
        } else if (size < SIZE_16) {
            hashcode = hashcode ^ (hashcode >>> 16);
        }
        return hashcode;
    }

}
