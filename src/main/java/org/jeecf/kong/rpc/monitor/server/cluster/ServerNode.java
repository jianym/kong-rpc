package org.jeecf.kong.rpc.monitor.server.cluster;

import java.util.Objects;

import lombok.Data;

/**
 * 服务节点
 * 
 * @author jianyiming
 *
 */
@Data
public class ServerNode {

    /**
     * ip 信息
     */
    private String ip = "";
    /**
     * 端口信息
     */
    private int port = 0;

    /**
     * 序列号 有序生成 用于选举
     */
    private String seq = "";
    /**
     * 1 主 2 从
     */
    private int state = 1;
    /**
     * 现有从节点个数，当前节点为主节点有效
     */
    private int slave = 0;
    /**
     * 主节点地址，当前节点为从节点有效
     */
    private String master = "";
    /**
     * 从节点地址，多个逗号分割，当前节点为主节点有效
     */
    private String slaves = "";
    /**
     * 写入数据条数，用于选举
     */
    private int number = 0;

    @Override
    public boolean equals(Object node) {
        ServerNode serverNode = (ServerNode) node;
        if (serverNode.getIp().equals(this.getIp()) && serverNode.getPort() == this.getPort()) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }

}
