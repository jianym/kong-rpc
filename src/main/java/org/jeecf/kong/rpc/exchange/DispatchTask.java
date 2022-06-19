package org.jeecf.kong.rpc.exchange;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.apache.commons.lang3.StringUtils;
import org.jeecf.common.mapper.JsonMapper;
import org.jeecf.kong.rpc.common.DistributeId;
import org.jeecf.kong.rpc.common.SpringContextUtils;
import org.jeecf.kong.rpc.common.ThreadContainer;
import org.jeecf.kong.rpc.common.exception.ArgsProcessingException;
import org.jeecf.kong.rpc.common.exception.ResourceLocationNotMatchException;
import org.jeecf.kong.rpc.common.exception.ResponseExceptionUtils;
import org.jeecf.kong.rpc.protocol.serializer.ConstantValue;
import org.jeecf.kong.rpc.protocol.serializer.MsgProtocol;
import org.jeecf.kong.rpc.protocol.serializer.Request;
import org.jeecf.kong.rpc.protocol.serializer.Response;
import org.jeecf.kong.rpc.protocol.serializer.Serializer;
import org.jeecf.kong.rpc.register.AfterHandlerContext;
import org.jeecf.kong.rpc.register.AroundHandlerContext;
import org.jeecf.kong.rpc.register.BeforeHandlerContext;
import org.jeecf.kong.rpc.register.ExceptionHandlerContext;
import org.jeecf.kong.rpc.register.ExceptionHandlerContext.ExceptionNode;
import org.jeecf.kong.rpc.register.ProviderContainer;
import org.jeecf.kong.rpc.register.ProviderContainer.RequestServerNode;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.databind.JsonNode;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务分发任务处理
 * 
 * @author jianyiming
 *
 */
@Slf4j
public class DispatchTask implements Runnable {

    private Request request = null;

    private byte serializer = 0;

    private ChannelHandlerContext ctx = null;

    private static final int RETRY = 3;

    private static final long SLEEP_MS = 100;

    private ProviderContainer providerContainer = ProviderContainer.getInstance();

    private BeforeHandlerContext beforeConntext = BeforeHandlerContext.getInstance();

    private AfterHandlerContext afterConntext = AfterHandlerContext.getInstance();

    private AroundHandlerContext aroundConntext = AroundHandlerContext.getInstance();

    private ExceptionHandlerContext exConntext = ExceptionHandlerContext.getInstance();

    public DispatchTask(Request request, ChannelHandlerContext ctx, byte serializer) {
        this.request = request;
        this.ctx = ctx;
        this.serializer = serializer;
    }

    @Override
    public void run() {
        String span = DistributeId.getId();
        String path = request.getPath();
        int version = request.getVersion();
        MsgProtocol msg = new MsgProtocol();
        Response response = new Response();
        response.setClientSpan(request.getClientSpan());
        response.setServerSpan(span);
        ThreadContainer threadContainer = SpringContextUtils.getBean(ThreadContainer.class);
        threadContainer.set(ThreadContainer.ID, request.getId());
        threadContainer.set(ThreadContainer.SPAN, span);
        Object result = null;
        Object[] arrayO = null;
        try {
            log.debug("server is receive,req={}", request);
            RequestServerNode node = getRequestServerNode();
            if (node == null) {
                throw new ResourceLocationNotMatchException("resource not found " + version + "_" + path);
            }
            Method m = node.getMethod();
            Parameter[] parameters = m.getParameters();
            if (parameters != null && parameters.length > 0) {
                arrayO = getArgs(parameters, request.getArgs());
            }
            try {
                beforeConntext.exec(arrayO, node, request, response);
                result = aroundConntext.exec(arrayO, node, request, response);
                afterConntext.exec(arrayO, result, node, request, response);
            } catch (Throwable e) {
                ExceptionNode exNode = exConntext.exec(arrayO, result, e, node, request, response);
                if (exNode == null) {
                    throw e;
                }
                result = exNode.getResult();
            }
            if (result != null) {
                response.setData(JsonMapper.toJson(result));
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            int code = ResponseExceptionUtils.getCode(e);
            response.setCode(code);
            response.setMessage(e.getMessage());
        } finally {
            threadContainer.remove();
            response.setSs(System.currentTimeMillis());
            byte[] content = Serializer.getSerializer(Serializer.getSerializer(serializer), response);
            msg.setSerializer(serializer);
            msg.setContentLength(content.length);
            msg.setContent(content);
            int i = 0;
            log.debug("server is send,req={},res={}", request, response);
            while (i < RETRY) {
                if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                    ChannelFuture serverFuture = ctx.channel().writeAndFlush(msg);
                    serverFuture.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (!future.isSuccess()) {
                                log.error("server send fail,req={},res={}", request, response);
                            }
                        }
                    });
                    return;
                }
                try {
                    log.warn("server is retry,num={},req={},res={}", i, request, response);
                    Thread.sleep(SLEEP_MS);
                } catch (InterruptedException e) {
                    log.warn(e.getMessage(), e);
                }
                i++;
            }
            log.error("server send fail,req={},res={}", request, response);

        }
    }

    public RequestServerNode getRequestServerNode() throws InstantiationException, IllegalAccessException {
        String path = request.getPath();
        int version = request.getVersion();
        byte transferMode = request.getTransferMode();
        RequestServerNode node = null;
        if (transferMode == ConstantValue.WHOLE_MODE)
            node = providerContainer.get(version + "_" + path);
        else {
            node = providerContainer.get(version + "_" + path + "_" + request.getClientId());
            if (node == null) {
                RequestServerNode tmpNode = providerContainer.get(version + "_" + path);
                node = providerContainer.new RequestServerNode();
                BeanUtils.copyProperties(tmpNode, node);
                node.setInstance(node.getInstance().getClass().newInstance());
                providerContainer.add(version + "_" + path + "_" + request.getClientId(), node, ConstantValue.SHARD_MODE);
            }
            if (transferMode == ConstantValue.SHARD_MODE_CLOSE) {
                providerContainer.removeShard(version + "_" + path + "_" + request.getClientId());
            }
        }
        return node;
    }

    private Object[] getArgs(Parameter[] parameters, String args) throws Exception {
        Object[] arrayO = new Object[parameters.length];
        if (StringUtils.isEmpty(args)) {
            throw new ArgsProcessingException("args is not match...");
        }
        JsonNode jsonNode = JsonMapper.getJsonNode(args);
        if (parameters.length == 1) {
            if (jsonNode == null) {
                arrayO[0] = null;
            } else {
                JsonNode pNode = jsonNode.get(parameters[0].getName());
                if (jsonNode.size() == 1 && pNode != null) {
                    arrayO[0] = JsonMapper.getInstance().treeToValue(pNode, parameters[0].getType());
                } else {
                    arrayO[0] = JsonMapper.getInstance().treeToValue(jsonNode, parameters[0].getType());
                }
            }
        } else {
            int i = 0;
            for (Parameter parameter : parameters) {
                String name = parameter.getName();
                if (jsonNode == null) {
                    arrayO[i] = null;
                } else {
                    JsonNode pNode = jsonNode.get(name);
                    if (pNode == null)
                        arrayO[i] = null;
                    else
                        arrayO[i] = JsonMapper.getInstance().treeToValue(pNode, parameter.getType());
                }
                i++;
            }
        }
        return arrayO;
    }

}
