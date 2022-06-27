package org.jeecf.kong.rpc.protocol.serializer;

import org.jeecf.kong.rpc.discover.KrpcClientContainer.RequestClientNode;
import org.jeecf.kong.rpc.protocol.serializer.RequestProto.Request.Builder;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * protobuf 序列化
 * 
 * @author jianyiming
 *
 */
public class ProtobufSerializerUtils {

    public static byte[] serialize(RequestClientNode req) {
        Builder request = RequestProto.Request.newBuilder()
                .setArgs(req.getArgs())
                .setClientId(req.getClientId())
                .setClientSpan(req.getClientSpan())
                .setId(req.getTraceId()).
                setPath(req.getPath())
                .setTime(req.getTime())
                .setVersion(req.getVersion())
                .setTransferMode(ByteString.copyFrom(new byte[] {req.getTransferMode()}));
        return request.build().toByteArray();
    }
    
    public static byte[] serialize(Response res) {
        org.jeecf.kong.rpc.protocol.serializer.ResponseProto.Response.Builder response = ResponseProto.Response.newBuilder()
                .setClientSpan(res.getClientSpan())
                .setData(res.getData())
                .setCode(res.getCode())
                .setMessage(res.getMessage())
                .setServerSpan(res.getServerSpan())
                .setSr(res.getSr())
                .setSs(res.getSs());
        return response.build().toByteArray();
    }

    public static Object deserializeToRequest(byte[] b) throws InvalidProtocolBufferException {
        if (b == null) {
            return null;
        }
        return RequestProto.Request.parseFrom(b);
    }
    
    public static Object deserializeToResponse(byte[] b) throws InvalidProtocolBufferException {
        if (b == null) {
            return null;
        }
        return ResponseProto.Response.parseFrom(b);
    }

}
