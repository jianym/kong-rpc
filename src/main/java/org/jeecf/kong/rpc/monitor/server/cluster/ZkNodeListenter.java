package org.jeecf.kong.rpc.monitor.server.cluster;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.jeecf.common.mapper.JsonMapper;
import org.jeecf.kong.rpc.center.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监控节点
 * 
 * @author jianyiming
 *
 */
@Component
public class ZkNodeListenter {

    @Autowired
    private Selector selector;

    public void load() throws Exception {
        CuratorFramework curator = ZkClient.getSingleCuratorFramework();
        watchServer(curator);
    }

    private void watchServer(CuratorFramework curator) throws Exception {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(curator, "/monitor/node", true);
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {

            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                PathChildrenCacheEvent.Type type = pathChildrenCacheEvent.getType();
                if (type.equals(PathChildrenCacheEvent.Type.CHILD_ADDED)) {
                    byte[] source = pathChildrenCacheEvent.getData().getData();
                    if (source != null) {
                        selector.updateSlaveInfo(JsonMapper.getInstance().readValue(source, ServerNode.class));
                    }
                } else if (type.equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
                    byte[] source = pathChildrenCacheEvent.getData().getData();
                    if (source != null) {
                        ServerNode node = JsonMapper.getInstance().readValue(source, ServerNode.class);
                        if (node.getState() == 1) {
                            selector.select(node);
                        } else {
                            selector.updateSlaveInfo(JsonMapper.getInstance().readValue(source, ServerNode.class));
                        }
                    }
                } else if (type.equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)) {
                    byte[] source = pathChildrenCacheEvent.getData().getData();
                    if (source != null) {
                        ServerNode node = JsonMapper.getInstance().readValue(source, ServerNode.class);
                        selector.updateMasterInfo(node);
                    }
                }
            }
        });
        pathChildrenCache.start();
    }

}
