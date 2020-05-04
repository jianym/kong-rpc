package org.jeecf.kong.rpc.protocol.serializer;

/**
 * 序列化
 * 
 * @author jianyiming
 *
 */
public enum Serializer {

    KYRO;

    public static byte getSerializer(Serializer serializer) {
        if (serializer == Serializer.KYRO) {
            return 1;
        }
        return -1;
    }

    public static Serializer getSerializer(byte serializer) {
        if (serializer == 1) {
            return KYRO;
        }
        return null;
    }

    public static byte[] getSerializer(Serializer serializer, Object obj) {
        if (serializer == Serializer.KYRO) {
            return KryoSerializerUtils.serialize(obj);
        }
        return null;
    }

    public static Object deserialize(int serializer, byte[] b) {
        if (serializer == 1) {
            return KryoSerializerUtils.deserialize(b);
        }
        return null;
    }

}
