package org.jeecf.kong.rpc.discover;

import org.jeecf.kong.rpc.protocol.serializer.ResponseSerializerHelper;

import lombok.Data;

/**
 * 上下文实体
 * 
 * @author jianyiming
 *
 */
@Data
public class ContextEntity {
    /**
     * 当前线程
     */
    private volatile Thread thread;
    /**
     * 响应数据
     */
    private volatile ResponseSerializerHelper responseHelper;

}
