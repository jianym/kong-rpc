package org.jeecf.kong.rpc.center;

import lombok.Data;

/**
 * 注册中心 节点
 * 
 * @author jianyiming
 *
 */
@Data
public class CenterNode {
    /**
     * 服务ip
     */
    private String ip;
    /**
     * 服务端口
     */
    private Integer port;

}
