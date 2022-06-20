package org.jeecf.kong.rpc.exchange;

import javax.net.ssl.SSLEngine;

import org.jeecf.kong.rpc.common.exception.NotExistSslEngineException;
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
            if (!node.isSsl())
                node.setNettyClient(new NettyClient(node.getTimeout(), node.getLow(), node.getHeight(), null));
            else {
                SSLEngine engine = SslSocketEngine.get(node.getName());
                if(engine == null)
                    throw new NotExistSslEngineException("not exist SSLEngine....");
                node.setNettyClient(new NettyClient(node.getTimeout(), node.getLow(), node.getHeight(), engine));
            }
            node.setState(ServerNode.STATE_OPEN);
            return node.getNettyClient();
        }
    }

}
