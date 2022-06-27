package org.jeecf.kong.rpc.protocol.serializer;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * request 序列化 帮助类
 * 
 * @author jianyiming
 *
 */
public class RequestSerializerHelper {

    private Request request;

    private RequestProto.Request requestProto;

    public RequestSerializerHelper(byte[] request, int serializer) throws InvalidProtocolBufferException {
        if (serializer == Serializer.KYRO.getCode()) {
            this.request = (Request) KryoSerializerUtils.deserialize(request);
        } else if (serializer == Serializer.PROTOBUF.getCode()) {
            this.requestProto = (RequestProto.Request) ProtobufSerializerUtils.deserializeToRequest(request);
        }
    }

    public Object get() {
        if (request != null)
            return request;
        return requestProto;
    }

    public String getId() {
        if (request != null)
            return request.getId();
        return requestProto.getId();
    }

    public int getVersion() {
        if (request != null)
            return request.getVersion();
        return requestProto.getVersion();
    }

    public String getClientSpan() {
        if (request != null)
            return request.getClientSpan();
        return requestProto.getClientSpan();
    }

    public String getArgs() {
        if (request != null)
            return request.getArgs();
        return requestProto.getArgs();
    }

    public String getClientId() {
        if (request != null)
            return request.getArgs();
        return requestProto.getArgs();
    }
    
    public String getPath() {
        if (request != null)
            return request.getPath();
        return requestProto.getPath();
    }
    
    public byte getTransferMode() {
        if (request != null)
            return request.getTransferMode();
        return requestProto.getTransferMode().byteAt(0);
    }

}
