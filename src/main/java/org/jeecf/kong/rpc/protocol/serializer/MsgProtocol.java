package org.jeecf.kong.rpc.protocol.serializer;

import lombok.Data;

/**
 * 信息协议
 * 
 * @author jianyiming
 *
 */
@Data
public class MsgProtocol {
     /**
      * 魔数
      */
    private int magic = ConstantValue.MAGIC;
    /**
     * 通信版本
     */
    private byte version = ConstantValue.VERSION;
    /**
     * 序列化方式
     */
    private byte serializer;
    /**
     * 内容长度
     */
    private int contentLength;
    /**
     * 内容
     */
    private byte[] content;

}
