package org.jeecf.kong.rpc.protocol.serializer;

import lombok.Data;

/**
 * 通信 请求实体
 * 
 * @author jianyiming
 *
 */
@Data
public class Request {
    /**
     * 全局通信id
     */
    private String id;
    /**
     * 当前通信id
     */
    private String clientSpan;
    /**
     * 客户端发送时自定义的id
     */
    private String clientId;
    /**
     * 资源版本
     */
    private Integer version = 0;
    /**
     * 传输模式
     */
    private byte transferMode;
    /**
     * 资源路径
     */
    private String path;
    /**
     * 参数
     */
    private String args;
    /**
     * 通信时间
     */
    private Long time;

}
