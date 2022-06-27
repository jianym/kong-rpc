package org.jeecf.kong.rpc.exchange;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.commons.lang3.StringUtils;
import org.jeecf.kong.rpc.common.exception.NoServerException;
import org.jeecf.kong.rpc.common.exception.NotExistSslEngineException;
import org.jeecf.kong.rpc.common.exception.ResponseExceptionUtils;
import org.jeecf.kong.rpc.common.exception.SocketException;
import org.jeecf.kong.rpc.common.exception.TimeoutException;
import org.jeecf.kong.rpc.discover.ConsumerContainer;
import org.jeecf.kong.rpc.discover.ConsumerContainer.ServerNode;
import org.jeecf.kong.rpc.discover.ContextContainer;
import org.jeecf.kong.rpc.discover.ContextEntity;
import org.jeecf.kong.rpc.discover.KrpcClientContainer.RequestClientNode;
import org.jeecf.kong.rpc.protocol.NettyClient;
import org.jeecf.kong.rpc.protocol.serializer.ConstantValue;
import org.jeecf.kong.rpc.protocol.serializer.MsgProtocol;
import org.jeecf.kong.rpc.protocol.serializer.ResponseSerializerHelper;
import org.jeecf.kong.rpc.protocol.serializer.Serializer;
import org.springframework.beans.BeanUtils;

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

    public Object send(RequestClientNode reqNode) throws Exception {
        ContextEntity entity = ContextContainer.getInstance().get(reqNode.getClientSpan());
        int retry = reqNode.getRetry();
        int timeout = reqNode.getTimeout();
        Serializer serializer = Serializer.KYRO;
        int i = 0;
        if (retry < 0) {
            retry = 0;
        }
        if (timeout <= 0) {
            timeout = WAIT_MS;
        }
        log.debug("client is send,req={}", reqNode);
        ServerNode server =  getTransferServerNode(reqNode);;
        if(server.getSerializer().equals("protobuf")) {
            serializer = Serializer.PROTOBUF;
        }
        MsgProtocol msg = new MsgProtocol();
        byte[] content = Serializer.getSerializer(serializer, reqNode);
        msg.setSerializer(Serializer.getSerializer(serializer));
        msg.setContentLength(content.length);
        msg.setContent(content);
        
        while (i <= retry) {
            if (i > 0) {
                log.warn("client is retry,num={},req={}", i, reqNode);
            }
            try {
                boolean send = server.getNettyClient().send(server.getIp(), server.getPort(), msg,reqNode.getClientSpan());
                if (!send) {
                    throw new SocketException("socket connection fail...");
                }
                long deadline = System.currentTimeMillis() + TimeUnit.MILLISECONDS.toMillis(timeout);
                LockSupport.parkUntil(deadline);
                if (entity.getResponseHelper() == null) {
                    throw new TimeoutException();
                } else {
                    ResponseSerializerHelper res = entity.getResponseHelper();
                    log.debug("client is receive,req={},res={}", reqNode, res.get());
                    ResponseExceptionUtils.throwException(res.getCode(), res.getMessage());
                    Object result = null;
                    if (StringUtils.isNotEmpty(res.getData())) {
                        ObjectMapper om = new ObjectMapper();
                        result = om.readValue(res.getData(), reqNode.getReturnType());
                    }
                    return result;
                }
            } catch (Exception e) {
                boolean isRetry = consumerContainer.getRetryManager().isRetry(e.getClass());
                if (isRetry && i < retry) {
                    log.error(e.getMessage());
                    i++;
                    server = getTransferServerNode(reqNode);
                    continue;
                }
                throw e;
            } finally {
                if (!reqNode.isKeepAlive()) {
                    if (server != null && server.getNettyClient() != null)
                        server.getNettyClient().close();
                }
            }

        }
        return null;
    }

    protected ServerNode getTransferServerNode(RequestClientNode reqNode) {
        int size = consumerContainer.size(reqNode.getAlias());
        if (size == 0) {
            throw new NoServerException("no server can connection...");
        }
        ServerNode server = null;
        if (reqNode.getTransferMode() == ConstantValue.WHOLE_MODE) {
            boolean keepAlive = reqNode.isKeepAlive();
            server = this.getServerNode(reqNode.getAlias(), reqNode.getArgs());
            if (reqNode.getArgs().getBytes().length >= server.getBytes() && server.getBytes() > 0) {
                keepAlive = false;
            }
            if (!keepAlive) {
                NettyClient client = null;
                if (!server.isSsl())
                    client = new NettyClient(server.getTimeout(), server.getLow(), server.getHeight(), null);
                else {
                    SslSocketEngine engine = ConsumerContainer.getInstance().getSslEngine();
                    if (engine == null || engine.get(server.getName()) == null)
                        throw new NotExistSslEngineException("not exist SSLEngine....");
                    client = new NettyClient(server.getTimeout(), server.getLow(), server.getHeight(), engine.get(server.getName()));
                }
                ServerNode newServer = consumerContainer.new ServerNode();
                BeanUtils.copyProperties(server, newServer);
                newServer.setNettyClient(client);
                server = newServer;
            }
        } else {
            if (reqNode.getTransferMode() == ConstantValue.SHARD_MODE) {
                reqNode.setKeepAlive(true);
                server = consumerContainer.get(reqNode.getClientId());
                if (server == null) {
                    server = this.getServerNode(reqNode.getAlias(), reqNode.getArgs());
                    NettyClient client = null;
                    if (!server.isSsl())
                        client = new NettyClient(server.getTimeout(), server.getLow(), server.getHeight(), null);
                    else {
                        SslSocketEngine engine = ConsumerContainer.getInstance().getSslEngine();
                        if (engine == null || engine.get(server.getName()) == null)
                            throw new NotExistSslEngineException("not exist SSLEngine....");
                        client = new NettyClient(server.getTimeout(), server.getLow(), server.getHeight(), engine.get(server.getName()));
                    }
                    ServerNode shardServer = consumerContainer.new ServerNode();
                    BeanUtils.copyProperties(server, shardServer);
                    shardServer.setNettyClient(client);
                    consumerContainer.putShard(reqNode.getClientId(), shardServer);
                }
            } else {
                server = consumerContainer.get(reqNode.getClientId());
                consumerContainer.remove(reqNode.getClientId());
                reqNode.setKeepAlive(false);
            }
        }
        return server;
    }

    /**
     * 获取服务端节点
     * 
     * @param alias
     * @param data
     * @return 服务节点
     */
    protected abstract ServerNode getServerNode(String alias, String data);

}
