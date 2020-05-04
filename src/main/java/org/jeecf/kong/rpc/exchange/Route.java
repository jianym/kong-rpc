package org.jeecf.kong.rpc.exchange;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.commons.lang3.StringUtils;
import org.jeecf.kong.rpc.common.exception.NoServerException;
import org.jeecf.kong.rpc.common.exception.ResponseExceptionUtils;
import org.jeecf.kong.rpc.common.exception.SocketException;
import org.jeecf.kong.rpc.common.exception.TimeoutException;
import org.jeecf.kong.rpc.discover.ConsumerContainer;
import org.jeecf.kong.rpc.discover.ConsumerContainer.ServerNode;
import org.jeecf.kong.rpc.discover.ContextContainer;
import org.jeecf.kong.rpc.discover.ContextEntity;
import org.jeecf.kong.rpc.discover.KrpcClientContainer.RequestClientNode;
import org.jeecf.kong.rpc.protocol.NettyClient;
import org.jeecf.kong.rpc.protocol.serializer.Request;
import org.jeecf.kong.rpc.protocol.serializer.Response;
import org.jeecf.kong.rpc.protocol.serializer.Serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 路由
 * 
 * @author jianyiming
 *
 */
@Slf4j
public abstract class Route {

    private static final int WAIT_MS = 3000;

    protected ConsumerContainer consumerContainer = ConsumerContainer.getInstance();

    public Object send(RequestClientNode reqNode, Request req) throws Exception {
        ContextEntity entity = ContextContainer.getInstance().get(req.getClientSpan());
        int retry = reqNode.getRetry();
        int timeout = reqNode.getTimeout();
        String alias = reqNode.getAlias();
        int i = 0;
        if (retry < 0) {
            retry = 0;
        }
        log.debug("client is send,req={}", req);
        while (i <= retry) {
            if (i > 0) {
                log.warn("client is retry,num={},req={}", i, req);
            }
            ServerNode server = getServerNode(alias, req.getArgs());
            if (server == null) {
                if (i == retry) {
                    throw new NoServerException("no server can connection...");
                }
                i++;
                continue;
            }
            NettyClient client = server.getNettyClient();
            boolean send = false;
            try {
                send = client.send(server.getIp(), server.getPort(), req, Serializer.KYRO);
                if (!send) {
                    if (i == retry) {
                        throw new SocketException("socket connection fail...");
                    }
                    i++;
                    continue;
                }
            } catch (InterruptedException e) {
                if (i == retry) {
                    throw e;
                }
                i++;
                continue;
            }
            if (timeout <= 0) {
                timeout = WAIT_MS;
            }
            long deadline = System.currentTimeMillis() + TimeUnit.MILLISECONDS.toMillis(timeout);
            LockSupport.parkUntil(deadline);
            if (entity.getResponse() == null) {
                if (i == retry) {
                    throw new TimeoutException();
                }
                i++;
                continue;
            } else {
                Response res = entity.getResponse();
                log.debug("client is receive,req={},res={}", req,res);
                ResponseExceptionUtils.throwException(res.getCode(), res.getMessage());
                Object result = null;
                if (StringUtils.isNotEmpty(res.getData())) {
                    ObjectMapper om = new ObjectMapper();
                    result = om.readValue(res.getData(), reqNode.getReturnType());
                }
                return result;
            }
        }
        return null;
    }

    /**
     * 获取服务端节点
     * 
     * @param alias
     * @param data
     * @return
     */
    protected abstract ServerNode getServerNode(String alias, String data);

}
