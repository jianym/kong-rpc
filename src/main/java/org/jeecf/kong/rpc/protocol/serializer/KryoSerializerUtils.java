package org.jeecf.kong.rpc.protocol.serializer;

import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

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

    public static byte[] serialize(Object msg) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Output output = new Output(bos);
        try {
            Kryo kryo = kryoSerializerUtils.getKryo();
            kryo.writeClassAndObject(output, msg);
            output.flush();
            return bos.toByteArray();
        } finally {
            output.close();
            output.close();
        }
    }

    public static Object deserialize(ByteBuf out) {
        if (out == null) {
            return null;
        }
        Input input = new Input(new ByteBufInputStream(out));
        Kryo kryo = kryoSerializerUtils.getKryo();
        return kryo.readClassAndObject(input);
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
