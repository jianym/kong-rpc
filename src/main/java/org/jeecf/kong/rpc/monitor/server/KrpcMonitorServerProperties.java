package org.jeecf.kong.rpc.monitor.server;

import org.jeecf.kong.rpc.center.ZkProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "krpc.monitor.server")
public class KrpcMonitorServerProperties {
    
    private Integer port = 20416;
    
    private Integer slave = 0;
    
    private ZkProperties zookeeper;

}
