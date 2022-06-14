package org.jeecf.kong.rpc.register;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.jeecf.kong.rpc.center.ZkClient;
import org.jeecf.kong.rpc.center.ZkProperties;
import org.springframework.beans.BeanUtils;

/**
 * zk注册
 * 
 * @author jianyiming
 *
 */
public class ZkRegister {

    private static CuratorFramework getClient(ZkProperties zK) {
        ZkProperties zkProperties = new ZkProperties();
        BeanUtils.copyProperties(zK, zkProperties);
        return ZkClient.initClient(zkProperties);
    }

    public static void register(String rootPath, String node, ZkProperties zK) throws Exception {
        CuratorFramework curator = getClient(zK);
        curator.start();
        ZkClient.creatingParentsIfNeeded(curator, CreateMode.EPHEMERAL, rootPath, node.getBytes());
        PathChildrenCache pathChildrenCache = new PathChildrenCache(curator, "/", false);
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                PathChildrenCacheEvent.Type type = pathChildrenCacheEvent.getType();
                if (type.equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
                    if (pathChildrenCacheEvent.getData().getPath().equals(rootPath))
                        ZkClient.create(curator, CreateMode.EPHEMERAL, rootPath, node.getBytes());
                }
            }
        });
        pathChildrenCache.start();
    }

}
