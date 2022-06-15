package org.jeecf.kong.rpc.monitor.server.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.CreateMode;
import org.jeecf.common.mapper.JsonMapper;
import org.jeecf.kong.rpc.center.ZkClient;
import org.jeecf.kong.rpc.monitor.server.KrpcMonitorServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 添加节点，选举主节点
 * 
 * @author jianyiming
 *
 */
@Component
public class Selector {

    private String monitorLock = "/monitor/lock";

    @Autowired
    private KrpcMonitorServerProperties serverProperties;

    public void register(ServerNode currNode) throws Exception {
        CuratorFramework curator = ZkClient.getSingleCuratorFramework(serverProperties.getZookeeper());
        int slave = serverProperties.getSlave();
        InterProcessMutex mutex = new InterProcessMutex(curator, monitorLock);
        try {
            mutex.acquire();
            if (slave <= 0) {
                // 当前节点为主节点
                currNode.setState(1);
                currNode.setMaster(currNode.getIp() + "-" + currNode.getIp());
                updateInfo(currNode, null, null);
            } else {
                List<ServerNode> serverNodes = getServerNodes();
                if (CollectionUtils.isEmpty(serverNodes)) {
                    // 当前节点为主节点
                    currNode.setState(1);
                    currNode.setMaster(currNode.getIp() + "-" + currNode.getIp());
                    updateInfo(currNode, null, null);
                } else {
                    boolean isSlave = false;
                    for (ServerNode masterNode : serverNodes) {
                        if (masterNode.getState() == 1 && masterNode.getSlave() < slave) {
                            // 当前节点为从节点
                            isSlave = true;
                            currNode.setState(2);
                            currNode.setMaster(masterNode.getMaster());
                            currNode.setSlave(masterNode.getSlave() + 1);
                            String slaves = masterNode.getSlaves();
                            List<ServerNode> slaveNodeList = new ArrayList<>();
                            if (StringUtils.isEmpty(slaves)) {
                                currNode.setSlaves(currNode.getIp() + "-" + currNode.getPort());
                                slaveNodeList.add(currNode);
                            } else {
                                currNode.setSlaves("," + currNode.getIp() + "-" + currNode.getPort());
                                for (ServerNode slaveNode : serverNodes) {
                                    if (slaveNode.getMaster().equals(masterNode.getMaster()) && slaveNode.getState() == 2) {
                                        slaveNode.setSlave(slaveNode.getSlave() + 1);
                                        slaveNode.setSlaves(slaveNode.getSlaves() + "," + currNode.getIp() + "-" + currNode.getPort());
                                        slaveNodeList.add(slaveNode);
                                    }
                                }
                                slaveNodeList.add(currNode);
                            }
                            updateInfo(currNode, masterNode.getSeq(), slaveNodeList);
                        }
                    }
                    if (!isSlave) {
                        // 当前节点为主节点
                        currNode.setState(1);
                        currNode.setMaster(currNode.getIp() + "-" + currNode.getIp());
                        updateInfo(currNode, null, null);
                    }
                }
            }
        } finally {
            mutex.release();
        }
    }

    /**
     * 当前主节点移除时，进行选举
     * 
     * @param masterNode
     * @throws Exception
     */
    public void select(ServerNode centerNode) throws Exception {
        if (!isMasterInfo(centerNode))
            return;
        CuratorFramework curator = ZkClient.getSingleCuratorFramework();
        InterProcessMutex mutex = new InterProcessMutex(curator, monitorLock);
        try {
            mutex.acquire();
            if (!isMasterInfo(centerNode))
                return;
            ClusterContext clusterContext = ClusterContext.getInstance();
            List<ServerNode> slaveNodes = clusterContext.getSlaveNodes();
            Collections.sort(slaveNodes, new Comparator<ServerNode>() {
                @Override
                public int compare(ServerNode node1, ServerNode node2) {
                    if (node1.getNumber() == node2.getNumber()) {
                        return Integer.parseInt(node1.getSeq()) - Integer.parseInt(node2.getSeq());
                    }
                    return node1.getNumber() - node2.getNumber();
                }
            });
            ServerNode mNode = slaveNodes.get(slaveNodes.size() - 1);
            if (mNode.equals(clusterContext.getNode())) {
                // 当前节点选为主节点
                ServerNode node = clusterContext.getNode();
                String address = node.getIp() + "-" + node.getPort();
                node.setMaster(address);
                node.setState(1);
                String[] slaves = node.getSlaves().split(",");
                node.setSlave(slaves.length - 1);
                String newSlaves = "";
                for (int i = 0; i < slaves.length; i++) {
                    if (!slaves[i].equals(address)) {
                        if (i == 0) {
                            newSlaves += slaves[i];
                        } else {
                            newSlaves += "," + slaves[i];
                        }
                    }
                }
                node.setSlaves(newSlaves);
                updateInfo(node, null, null);
            }
        } finally {
            mutex.release();
        }

    }

    private List<ServerNode> getServerNodes() throws Exception {
        List<ServerNode> nodeList = new ArrayList<>();
        CuratorFramework curator = ZkClient.getSingleCuratorFramework();
        List<String> pathList = ZkClient.children(curator, "/monitor/node");
        for (String path : pathList) {
            byte[] b = ZkClient.get(curator, path);
            nodeList.add(JsonMapper.getInstance().readValue(b, ServerNode.class));
        }
        return nodeList;
    }

    /**
     * 更新当前节点信息
     * 
     * @param masterNode
     * @throws Exception
     */
    private void updateInfo(ServerNode node, String seq, List<ServerNode> slaveNodes) throws Exception {
        ClusterContext clusterContext = ClusterContext.getInstance();
        CuratorFramework curator = ZkClient.getSingleCuratorFramework();
        clusterContext.setNode(node);
        List<ServerNode> slaveList = clusterContext.getSlaveNodes();
        if (node.getState() == 1) {
            // 作为主节点加入或被选为主节点
            clusterContext.setMasterNode(node);
            if (CollectionUtils.isNotEmpty(slaveList)) {
                Iterator<ServerNode> nodeIter = slaveList.iterator();
                if (nodeIter.hasNext()) {
                    ServerNode slaveNode = nodeIter.next();
                    if (slaveNode.equals(node)) {
                        nodeIter.remove();
                    }
                }
            }
            if (StringUtils.isEmpty(node.getSeq())) {
                // 作为主节点加入
                String resultPath = nodeResgiter("/monitor/node/" + node.getIp() + "-" + node.getPort() + "_", JsonMapper.toJson(node));
                node.setSeq(resultPath.split("_")[1]);
                ZkClient.set(curator, "/monitor/node/" + node.getIp() + "-" + node.getPort() + "_" + node.getSeq(), JsonMapper.toJson(node).getBytes());
            } else
                // 被选举为主节点
                ZkClient.set(curator, "/monitor/node/" + node.getIp() + "-" + node.getPort() + "_" + node.getSeq(), JsonMapper.toJson(node).getBytes());
        } else {
            // 作为从节点加入
            ServerNode masterNode = new ServerNode();
            String[] address = node.getMaster().split("-");
            String[] slaves = node.getSlaves().split(",");
            masterNode.setState(1);
            masterNode.setMaster(address[0] + "-" + address[1]);
            masterNode.setIp(address[0]);
            masterNode.setPort(Integer.valueOf(address[1]));
            masterNode.setSlave(slaves.length);
            masterNode.setSeq(seq);
            masterNode.setSlaves(node.getSlaves());
            clusterContext.setMasterNode(masterNode);
            clusterContext.setSlaveNodes(slaveNodes);
            String resultPath = nodeResgiter("/monitor/node/" + node.getIp() + "-" + node.getPort() + "_", JsonMapper.toJson(node));
            node.setSeq(resultPath.split("_")[1]);
            ZkClient.set(curator, "/monitor/node/" + node.getIp() + "-" + node.getPort() + "_" + node.getSeq(), JsonMapper.toJson(node).getBytes());

        }

    }

    /**
     * 判断节点是否为当前节点的主节点
     * 
     * @param masterNode
     */
    private boolean isMasterInfo(ServerNode centerNode) {
        String slaves = centerNode.getSlaves();
        ClusterContext clusterContext = ClusterContext.getInstance();
        String address = clusterContext.getNode().getIp() + "-" + clusterContext.getNode().getPort();
        if (centerNode.getState() == 1 && slaves.contains(address)) {
            return true;
        }
        return false;
    }

    /**
     * 当前节点的主节点变更时
     * 
     * @param masterNode
     * @throws Exception
     */
    public void updateMasterInfo(ServerNode centerNode) throws Exception {
        ClusterContext clusterContext = ClusterContext.getInstance();
        if (!isMasterInfo(centerNode) || centerNode.equals(clusterContext.getMasterNode()))
            return;
        CuratorFramework curator = ZkClient.getSingleCuratorFramework();
        InterProcessMutex mutex = new InterProcessMutex(curator, monitorLock);
        try {
            mutex.acquire();
            if (!isMasterInfo(centerNode) || centerNode.equals(clusterContext.getMasterNode()))
                return;
            ServerNode currNode = clusterContext.getNode();
            currNode.setMaster(centerNode.getMaster());
            clusterContext.setMasterNode(centerNode);
            List<ServerNode> slaveList = clusterContext.getSlaveNodes();

            String centerAdress = centerNode.getIp() + "-" + centerNode.getPort();
            if (currNode.getSlaves().contains(centerAdress)) {
                currNode.setSlave(currNode.getSlave() - 1);
                String[] slaves = currNode.getSlaves().split(",");
                String newSlaves = "";
                for (int i = 0; i < slaves.length; i++) {
                    if (!slaves[i].equals(centerAdress)) {
                        if (i == 0) {
                            newSlaves += slaves[i];
                        } else {
                            newSlaves += "," + slaves[i];
                        }
                    }
                }
                currNode.setSlaves(newSlaves);
            }
            if (CollectionUtils.isNotEmpty(slaveList)) {
                Iterator<ServerNode> nodeIter = slaveList.iterator();
                if (nodeIter.hasNext()) {
                    ServerNode slaveNode = nodeIter.next();
                    if (slaveNode.equals(centerNode)) {
                        nodeIter.remove();
                    }
                }
            }
            ZkClient.set(curator, "/monitor/node/" + currNode.getIp() + "-" + currNode.getPort() + "_" + currNode.getSeq(), JsonMapper.toJson(currNode).getBytes());
        } finally {
            mutex.release();
        }
    }

    /**
     * 当前节点的从节点 移除或添加时
     * 
     * @param slaveNode
     * @throws Exception 
     */
    public void updateSlaveInfo(ServerNode centerNode) throws Exception {
        ClusterContext clusterContext = ClusterContext.getInstance();
        ServerNode currNode = clusterContext.getNode();
        String centerAddress =centerNode.getIp()+"-"+centerNode.getPort();
        String currAddress =currNode.getIp()+"-"+currNode.getPort();
        if (centerNode.getState() == 1 || !centerNode.getSlaves().contains(currAddress))
            return;
        CuratorFramework curator = ZkClient.getSingleCuratorFramework();
        InterProcessMutex mutex = new InterProcessMutex(curator, monitorLock);
        try {
            mutex.acquire();
            if (centerNode.getState() == 1 || !centerNode.getSlaves().contains(currAddress))
                return;
            List<ServerNode> slaveList = clusterContext.getSlaveNodes();
            if(clusterContext.getNode().getSlaves().contains(centerAddress)) {
                currNode.setSlave(currNode.getSlave() - 1);
                String[] slaves = currNode.getSlaves().split(",");
                String newSlaves = "";
                for (int i = 0; i < slaves.length; i++) {
                    if (!slaves[i].equals(centerAddress)) {
                        if (i == 0) {
                            newSlaves += slaves[i];
                        } else {
                            newSlaves += "," + slaves[i];
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(slaveList)) {
                    Iterator<ServerNode> nodeIter = slaveList.iterator();
                    if (nodeIter.hasNext()) {
                        ServerNode slaveNode = nodeIter.next();
                        if (slaveNode.equals(centerNode)) {
                            nodeIter.remove();
                        }
                    }
                }
                currNode.setSlaves(newSlaves);
                ZkClient.set(curator, "/monitor/node/" + currAddress + "_" + currNode.getSeq(), JsonMapper.toJson(currNode).getBytes());
            } else {
                currNode.setSlave(currNode.getSlave() + 1);
                currNode.setSlaves(currNode.getSlaves()+","+centerAddress);
                slaveList.add(centerNode);
                ZkClient.set(curator, "/monitor/node/" + currAddress + "_" + currNode.getSeq(), JsonMapper.toJson(currNode).getBytes());
            }
        } finally {
            mutex.release();
        }
    }

    private String nodeResgiter(String rootPath, String node) throws Exception {
        CuratorFramework curator = ZkClient.getSingleCuratorFramework();
        // 建立临时序列节点，用作选举
        String resultPath = ZkClient.creatingParentsIfNeeded(curator, CreateMode.EPHEMERAL_SEQUENTIAL, rootPath, node.getBytes());
        PathChildrenCache pathChildrenCache = new PathChildrenCache(curator, "/monitor/node", false);
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
        return resultPath;
    }

}
