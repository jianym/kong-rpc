package org.jeecf.kong.rpc.register;

import org.jeecf.kong.rpc.center.ZkProperties;
import org.jeecf.kong.rpc.protocol.properties.ServerSocketProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 服务端配置文件
 * 
 * @author jianyiming
 *
 */
@Data
@Component
@ConfigurationProperties(prefix = "krpc.server")
public class KrpcServerProperties {

    private Integer port = 20415;
    
    private String name = "";
    
    private ThreadProperties thread = new ThreadProperties();

    private ServerSocketProperties socket = new ServerSocketProperties();

    private ZkProperties zookeeper;

    public class ThreadProperties {

        private int core = 10;

        private int queue = Integer.MAX_VALUE;

        public int getCore() {
            return core;
        }

        public void setCore(int core) {
            this.core = core;
        }

        public int getQueue() {
            return queue;
        }

        public void setQueue(int queue) {
            this.queue = queue;
        }

    }

}
