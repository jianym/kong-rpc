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
     * 资源版本
     */
    private Integer version = 0;
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
