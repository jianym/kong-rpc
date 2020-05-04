package org.jeecf.kong.rpc.exchange;

import org.jeecf.kong.rpc.discover.ConsumerContainer.ServerNode;
import org.jeecf.kong.rpc.protocol.NettyClient;

/**
 * 获取nettyClient 工厂
 * 
 * @author jianyiming
 *
 */
public class NettyClientFactory {

    public static NettyClient getInstance(ServerNode node) {
        if (node == null) {
            return null;
        }
        if (node.getState() == ServerNode.STATE_OPEN) {
            return node.getNettyClient();
        }
        synchronized (NettyClientFactory.class) {
            if (node.getState() == ServerNode.STATE_OPEN) {
                return node.getNettyClient();
            }
            node.setNettyClient(new NettyClient(node.getTimeout(),node.getLow(),node.getHeight()));
            node.setState(ServerNode.STATE_OPEN);
            return node.getNettyClient();
        }
    }

}
