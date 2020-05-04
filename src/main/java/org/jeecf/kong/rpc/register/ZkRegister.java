package org.jeecf.kong.rpc.register;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.jeecf.common.mapper.JsonMapper;
import org.jeecf.kong.rpc.center.ZkClient;
import org.jeecf.kong.rpc.center.CenterNode;
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
        ZkClient.create(curator, CreateMode.EPHEMERAL, "/server" + "-" + ip + "-" + port, JsonMapper.toJson(zkNode).getBytes());

    }

}
