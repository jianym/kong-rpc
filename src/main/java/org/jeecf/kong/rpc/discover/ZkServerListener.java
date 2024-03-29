package org.jeecf.kong.rpc.discover;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.jeecf.common.lang.StringUtils;
import org.jeecf.common.mapper.JsonMapper;
import org.jeecf.kong.rpc.center.CenterNode;
import org.jeecf.kong.rpc.center.ZkClient;
import org.jeecf.kong.rpc.center.ZkProperties;
import org.jeecf.kong.rpc.common.exception.NotFoundAliasException;
import org.jeecf.kong.rpc.discover.ConsumerContainer.ServerNode;
import org.jeecf.kong.rpc.discover.properties.KrpcClientProperties;
import org.jeecf.kong.rpc.discover.properties.KrpcProperties;
import org.jeecf.kong.rpc.exchange.NettyClientFactory;
import org.jeecf.kong.rpc.protocol.NettyClient;
import org.jeecf.kong.rpc.protocol.properties.SocketProperties;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * zk 服务端监听
 * 
 * @author jianyiming
 *
 */
@Component
public class ZkServerListener {

    @Autowired
    private KrpcClientProperties krpcClientProperties;

    private ConsumerContainer consumerContainer = ConsumerContainer.getInstance();

    private Map<String, CuratorFramework> getClients() {
        Map<String, CuratorFramework> curatorMap = new ConcurrentHashMap<>();

        ZkProperties zkProperties = new ZkProperties();
        BeanUtils.copyProperties(krpcClientProperties.getZookeeper(), zkProperties);
        if (StringUtils.isEmpty(krpcClientProperties.getName())) {
            throw new NotFoundAliasException("alias no exits...");
        }
        curatorMap.put(krpcClientProperties.getName(), ZkClient.initClient(zkProperties));
        List<KrpcProperties> proList = krpcClientProperties.getAlias();
        if (CollectionUtils.isNotEmpty(proList)) {
            proList.forEach(action -> {
                ZkProperties zkAliasProperties = new ZkProperties();
                BeanUtils.copyProperties(action.getZookeeper(), zkAliasProperties);
                if (StringUtils.isEmpty(action.getName())) {
                    throw new NotFoundAliasException("alias no exits...");
                }
                curatorMap.put(action.getName(), ZkClient.initClient(zkAliasProperties));
            });
        }
        return curatorMap;
    }

    public void load() throws Exception {
        Map<String, CuratorFramework> curatorMap = getClients();
        for (Map.Entry<String, CuratorFramework> entry : curatorMap.entrySet()) {
            String name = entry.getKey();
            CuratorFramework curator = entry.getValue();
            curator.start();
            watchServer(name, curator);
        }
    }

    private void watchServer(String alias, CuratorFramework curator) throws Exception {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(curator, "/server", true);
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {

            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                PathChildrenCacheEvent.Type type = pathChildrenCacheEvent.getType();
                if (type.equals(PathChildrenCacheEvent.Type.CHILD_ADDED)) {
                    byte[] source = pathChildrenCacheEvent.getData().getData();
                    if (source != null) {
                        String data = new String(source);
                        CenterNode zkNode = JsonMapper.getInstance().readValue(data, CenterNode.class);
                        String path = zkNode.getIp() + "-" + zkNode.getPort();
                        if (isRemoteNode(zkNode, alias)) {
                            if (krpcClientProperties.getName().equals(alias))
                                consumerContainer.put(zkNode.getName(), path, buildServerNode(zkNode, krpcClientProperties.getSocket(), krpcClientProperties.isSsl()));
                            else {
                                KrpcProperties krpcProperties = getKrpcProperties(alias);
                                if (krpcProperties != null) {
                                    consumerContainer.put(zkNode.getName(), path, buildServerNode(zkNode, krpcProperties.getSocket(), krpcProperties.isSsl()));
                                }
                            }
                        }
                    }
                } else if (type.equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
                    byte[] source = pathChildrenCacheEvent.getData().getData();
                    if (source != null) {
                        String data = new String(source);
                        CenterNode zkNode = JsonMapper.getInstance().readValue(data, CenterNode.class);
                        String path = zkNode.getIp() + "-" + zkNode.getPort();
                        if (isRemoteNode(zkNode, alias)) {
                            consumerContainer.remove(zkNode.getName(), path);
                        }
                    }
                }
            }
        });
        pathChildrenCache.start();
    }

    public KrpcProperties getKrpcProperties(String alias) {
        List<KrpcProperties> proList = krpcClientProperties.getAlias();
        if (CollectionUtils.isNotEmpty(proList)) {
            for (KrpcProperties properties : proList) {
                if (properties.getName().equals(alias)) {
                    return properties;
                }
            }
        }
        return null;
    }

    /**
     * 判断服务节点是否为当前客户端zk 通信节点
     * 
     * @param zkNode
     * @return
     */
    private boolean isRemoteNode(CenterNode zkNode, String alias) {
        if (zkNode.getName().equals(alias)) {
            return true;
        }
        return false;
    }

    private ServerNode buildServerNode(CenterNode zkNode, SocketProperties properties, boolean ssl) throws Exception {
        ServerNode node = consumerContainer.new ServerNode();
        node.setState(ServerNode.STATE_INIT);
        node.setIp(zkNode.getIp());
        node.setPort(Integer.valueOf(zkNode.getPort()));
        node.setTimeout(properties.getTimeout());
        node.setLow(properties.getLow());
        node.setHeight(properties.getHeight());
        node.setBytes(properties.getBytes());
        node.setSsl(ssl);
        node.setName(zkNode.getName());
        if (properties.isConnect()) {
            NettyClient client = NettyClientFactory.getInstance(node);
            client.connection(node.getIp(), node.getPort());
            node.setNettyClient(client);
        }
        return node;
    }

}
