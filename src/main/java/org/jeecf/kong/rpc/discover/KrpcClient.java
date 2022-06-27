package org.jeecf.kong.rpc.discover;

import java.util.concurrent.Future;

import org.jeecf.common.lang.StringUtils;
import org.jeecf.kong.rpc.common.ResourceLocationUtils;
import org.jeecf.kong.rpc.discover.KrpcClientContainer.RequestClientNode;

/**
 * 客户端 调用入口
 * 
 * @author jianyiming
 *
 */
public class KrpcClient {

    private int retry = 2;

    private String alias;

    private String basePath;

    private int timeout;

    private KrpcClient() {
    }

    protected void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    protected void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    protected void setRetry(int retry) {
        this.retry = retry;
    }

    protected void setAlias(String alias) {
        this.alias = alias;
    }

    public String execution(String path, String jsonData) throws Throwable {
        return this.execution(path, 0, jsonData, String.class);
    }

    public Future<String> executionAsync(String path, String jsonData) throws Throwable {
        return this.executionAsync(path, 0, jsonData, String.class);
    }

    public <T> T execution(String path, int version, String jsonData, Class<T> returnType) throws Throwable {
        if (StringUtils.isNotEmpty(this.basePath))
            path = ResourceLocationUtils.buildPath(this.basePath, path);
        RequestClientNode requestClientNode = buildClientNode(path, version, returnType);
        requestClientNode.setArgs(jsonData);
        return KrpcClientRun.runSync(requestClientNode);
    }

    public <T> Future<T> executionAsync(String path, int version, String jsonData, Class<T> returnType) throws Throwable {
        if (StringUtils.isNotEmpty(this.basePath))
            path = ResourceLocationUtils.buildPath(this.basePath, path);
        RequestClientNode requestClientNode = buildClientNode(path, version, returnType);
        requestClientNode.setArgs(jsonData);
        return KrpcClientRun.run(requestClientNode);
    }

    private <T> RequestClientNode buildClientNode(String path, int version, Class<T> returnType) {
        RequestClientNode requestClientNode = KrpcClientContainer.getInstance().new RequestClientNode();
        requestClientNode.setAlias(this.alias);
        requestClientNode.setPath(path);
        requestClientNode.setVersion(version);
        requestClientNode.setTimeout(this.timeout);
        requestClientNode.setRetry(this.retry);
        requestClientNode.setReturnType(returnType);
        return requestClientNode;
    }

    public static Builder builder() {
        return new Builder(new KrpcClient());
    }

    public static class Builder {

        private KrpcClient client;

        public Builder(KrpcClient client) {
            this.client = client;

        }

        public Builder setRetry(int retry) {
            this.client.setRetry(retry);
            return this;
        }

        public Builder setAlias(String alias) {
            this.client.setAlias(alias);
            return this;
        }

        public Builder setBasePath(String basePath) {
            this.client.setBasePath(basePath);
            return this;
        }

        public Builder setTimeout(int timeout) {
            this.client.setTimeout(timeout);
            return this;
        }

        public KrpcClient build() {
            return this.client;
        }
    }

}
