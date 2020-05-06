package org.jeecf.kong.rpc.register;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.jeecf.common.mapper.JsonMapper;
import org.jeecf.kong.rpc.center.CenterNode;
import org.jeecf.kong.rpc.center.ZkClient;
import org.jeecf.kong.rpc.center.ZkProperties;
import org.jeecf.kong.rpc.common.DistributeId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * zk注册
 * 
 * @author jianyiming
 *
 */
@Component
public class ZkRegister {

    @Autowired
    private KrpcServerProperties properties;

    private CuratorFramework getClient() {
        ZkProperties zkProperties = new ZkProperties();
        BeanUtils.copyProperties(properties.getZookeeper(), zkProperties);
        return ZkClient.initClient(zkProperties);
    }

    public void register() throws Exception {
        CuratorFramework curator = getClient();
        String ip = DistributeId.getLocalHostLANAddress().getHostAddress();
        Integer port = properties.getPort();
        curator.start();
        CenterNode zkNode = new CenterNode();
        zkNode.setIp(ip);
        zkNode.setPort(port);
        String path = "/server" + "-" + ip + "-" + port;
        ZkClient.create(curator, CreateMode.EPHEMERAL, path, JsonMapper.toJson(zkNode).getBytes());
        PathChildrenCache pathChildrenCache = new PathChildrenCache(curator, "/", false);
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                PathChildrenCacheEvent.Type type = pathChildrenCacheEvent.getType();
                if (type.equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
                    if (pathChildrenCacheEvent.getData().getPath().equals(path))
                        ZkClient.create(curator, CreateMode.EPHEMERAL, path, JsonMapper.toJson(zkNode).getBytes());
                }
            }
        });
        pathChildrenCache.start();
    }

}
