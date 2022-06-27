package org.jeecf.kong.rpc.protocol.serializer;

import org.jeecf.kong.rpc.discover.KrpcClientContainer.RequestClientNode;

/**
 * 序列化
 * 
 * @author jianyiming
 *
 */
public enum Serializer {

    KYRO(1), PROTOBUF(2);
    private int code;

    Serializer(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static byte getSerializer(Serializer serializer) {
        if (serializer == Serializer.KYRO) {
            return 1;
        } else if (serializer == Serializer.PROTOBUF) {
            return 2;
        }
        return -1;
    }

    public static Serializer getSerializer(byte serializer) {
        if (serializer == 1) {
            return KYRO;
        } else if (serializer == 2) {
            return PROTOBUF;
        }
        return null;
    }

    public static byte[] getSerializer(Serializer serializer, RequestClientNode req) {
        if (serializer == Serializer.KYRO) {
            return KryoSerializerUtils.serialize(req);
        } else if (serializer == Serializer.PROTOBUF) {
            return ProtobufSerializerUtils.serialize(req);
        }
        return null;
    }

    public static byte[] getSerializer(Serializer serializer, Response res) {
        if (serializer == Serializer.KYRO) {
            return KryoSerializerUtils.serialize(res);
        } else if (serializer == Serializer.PROTOBUF) {
            return ProtobufSerializerUtils.serialize(res);
        }
        return null;
    }

}
