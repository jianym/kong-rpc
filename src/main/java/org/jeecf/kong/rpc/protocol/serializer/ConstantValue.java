package org.jeecf.kong.rpc.protocol.serializer;

/**
 * 协议公用参数
 * 
 * @author jianyiming
 *
 */
public class ConstantValue {
    
    public static final int MAGIC = 0x825;

    public static final int VERSION = 1;
    
    public static final byte WHOLE_MODE = 0;
    
    public static final byte SHARD_MODE = 1;
    
    public static final byte SHARD_MODE_CLOSE = 2;
    

}
