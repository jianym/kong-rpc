package org.jeecf.kong.rpc.monitor.server.cluster;

import java.util.List;

/**
 * 集群容器
 * 
 * @author jianyiming
 *
 */
public class ClusterContext {

    private volatile static ClusterContext clusterContext = null;

    private ClusterContext() {
    }

    public static ClusterContext getInstance() {
        if (clusterContext != null)
            return clusterContext;
        synchronized (ClusterContext.class) {
            if (clusterContext != null)
                return clusterContext;
            return clusterContext = new ClusterContext();
        }
    }

    private ServerNode node = null;

    private ServerNode masterNode = null;

    private List<ServerNode> slaveNodes = null;

    public static void setClusterContext(ClusterContext clusterContext) {
        ClusterContext.clusterContext = clusterContext;
    }

    public ServerNode getNode() {
        return node;
    }

    public void setNode(ServerNode node) {
        this.node = node;
    }

    public ServerNode getMasterNode() {
        return masterNode;
    }

    public void setMasterNode(ServerNode masterNode) {
        this.masterNode = masterNode;
    }

    public List<ServerNode> getSlaveNodes() {
        return slaveNodes;
    }

    public void setSlaveNodes(List<ServerNode> slaveNodes) {
        this.slaveNodes = slaveNodes;
    }

}
