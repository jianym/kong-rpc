package org.jeecf.kong.rpc.protocol.serializer;

import java.io.ByteArrayOutputStream;

import org.jeecf.kong.rpc.discover.KrpcClientContainer.RequestClientNode;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * kryo 序列化工具
 * 
 * @author jianyiming
 *
 */
public class KryoSerializerUtils {

    private static KryoSerializerUtils kryoSerializerUtils = createInstance();

    private KryoSerializerUtils() {
    };

    public static KryoSerializerUtils createInstance() {
        return new KryoSerializerUtils();
    }

    private final ThreadLocal<Kryo> holder = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            return createKryo();
        }
    };

    public Kryo getKryo() {
        return holder.get();
    }

    public void removeKryo() {
        holder.remove();
    }

    public Kryo createKryo() {
        Kryo kryo = new Kryo();
        kryo.register(Request.class);
        kryo.register(Response.class);
        return kryo;
    }
    

    public static byte[] serialize(RequestClientNode reqNode) {
        Request req  = new Request();
        req.setId(reqNode.getTraceId());
        req.setArgs(reqNode.getArgs());
        req.setClientId(reqNode.getClientId());
        req.setClientSpan(reqNode.getClientSpan());
        req.setPath(reqNode.getPath());
        req.setTime(reqNode.getTime());
        req.setTransferMode(reqNode.getTransferMode());
        req.setVersion(reqNode.getVersion());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Output output = new Output(bos);
        try {
            Kryo kryo = kryoSerializerUtils.getKryo();
            kryo.writeClassAndObject(output, req);
            output.flush();
            return bos.toByteArray();
        } finally {
            output.close();
            output.close();
        }
    }
    
    public static byte[] serialize(Response res) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Output output = new Output(bos);
        try {
            Kryo kryo = kryoSerializerUtils.getKryo();
            kryo.writeClassAndObject(output, res);
            output.flush();
            return bos.toByteArray();
        } finally {
            output.close();
            output.close();
        }
    }


    public static Object deserialize(byte[] b) {
        if (b == null) {
            return null;
        }
        Kryo kryo = kryoSerializerUtils.getKryo();
        Input input = new Input(b);
        return kryo.readClassAndObject(input);
    }

}
