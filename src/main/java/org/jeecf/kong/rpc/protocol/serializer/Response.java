package org.jeecf.kong.rpc.protocol.serializer;

import lombok.Data;

/**
 * 通信响应实体
 * 
 * @author jianyiming
 *
 */
@Data
public class Response {
    
    /**
     * 调用方id
     */
    private String clientSpan;
    /**
     * 当前id
     */
    private String serverSpan;
    /**
     * 响应码
     */
    private Integer code = 0;
    /**
     * 响应消息
     */
    private String message;
    /**
     * 响应数据
     */
    private String data;
    /**
     * 通信时间
     */
    private Long sr;
    /**
     * 通信时间
     */
    private Long ss;

}
