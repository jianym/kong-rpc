package org.jeecf.kong.rpc.protocol.serializer;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * 响应序列化帮助类
 * 
 * @author jianyiming
 *
 */
public class ResponseSerializerHelper {

    private Response response = null;

    private ResponseProto.Response responseProto = null;

    public ResponseSerializerHelper(byte[] response, int serializer) throws InvalidProtocolBufferException {
        if (serializer == Serializer.KYRO.getCode()) {
            this.response = (Response) KryoSerializerUtils.deserialize(response);
        } else if (serializer == Serializer.PROTOBUF.getCode()) {
            this.responseProto = (ResponseProto.Response) ProtobufSerializerUtils.deserializeToResponse(response);
        }
    }

    public Object get() {
        if (response != null)
            return response;
        return responseProto;
    }

    public String getClientSpan() {
        if (response != null)
            return response.getClientSpan();
        return responseProto.getClientSpan();
    }

    public String getServerSpan() {
        if (response != null)
            return response.getServerSpan();
        return responseProto.getServerSpan();
    }

    public int getCode() {
        if (response != null)
            return response.getCode();
        return responseProto.getCode();
    }

    public String getMessage() {
        if (response != null)
            return response.getMessage();
        return responseProto.getMessage();
    }

    public String getData() {
        if (response != null)
            return response.getData();
        return responseProto.getData();
    }

    public Long getSr() {
        if (response != null)
            return response.getSr();
        return responseProto.getSr();
    }

    public Long getSs() {
        if (response != null)
            return response.getSs();
        return responseProto.getSs();
    }

}
