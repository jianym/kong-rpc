package org.jeecf.kong.rpc.discover;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import org.jeecf.common.mapper.JsonMapper;
import org.jeecf.kong.rpc.common.exception.NotExistKrpcException;
import org.jeecf.kong.rpc.discover.KrpcClientContainer.RequestClientNode;
import org.jeecf.kong.rpc.exchange.ShardData;
import org.jeecf.kong.rpc.protocol.serializer.ConstantValue;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * 客户端代理
 * 
 * @author jianyiming
 *
 */
public class ClientProxyInterceptor implements MethodInterceptor {

    private KrpcClientContainer container = KrpcClientContainer.getInstance();

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        RequestClientNode clientNode = container.get(obj.getClass().getName() + "_" + method.getName());
        if (clientNode == null) {
            throw new NotExistKrpcException("@krpc is not exist ..." + method);
        }
        String jsonData = null;
        Method m = clientNode.getMethod();
        if (args[0] instanceof ShardData) {
            ShardData data = (ShardData) args[0];
            jsonData = data.getJsonData();
            clientNode.setClientId(data.getClientId());
            if (!data.isClose()) {
                clientNode.setTransferMode(ConstantValue.SHARD_MODE);
            } else {
                clientNode.setTransferMode(ConstantValue.SHARD_MODE_CLOSE);
            }
        } else {
            if (args != null && args.length > 0) {
                Parameter[] ps = m.getParameters();
                Map<String, Object> argMap = new HashMap<>();
                for (int i = 0; i < args.length; i++) {
                    argMap.put(ps[i].getName(), args[i]);
                }
                jsonData = JsonMapper.toJson(argMap);
            }
        }
        if (clientNode.isSync())
            return KrpcClientRun.runSync(jsonData, clientNode);
        return KrpcClientRun.run(jsonData, clientNode);
    }

}
