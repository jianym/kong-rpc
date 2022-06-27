package org.jeecf.kong.rpc.discover;

import java.util.concurrent.Future;

import org.jeecf.kong.rpc.common.ResourceLocationUtils;
import org.jeecf.kong.rpc.common.SpringContextUtils;
import org.jeecf.kong.rpc.common.exception.ResourceLocationFormatException;
import org.jeecf.kong.rpc.discover.KrpcClientContainer.RequestClientNode;
import org.jeecf.kong.rpc.discover.properties.KrpcClientProperties;
import org.jeecf.kong.rpc.discover.properties.KrpcClientProperties.CircuitBreakerProperties;
import org.jeecf.kong.rpc.exchange.RouteHystrixCommand;

import com.netflix.hystrix.HystrixCommand.Setter;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;

/**
 * KrpcClient 启动类
 * 
 * @author jianyiming
 *
 */
public class KrpcClientRun {

    private static KrpcClientProperties properties = SpringContextUtils.getBean(KrpcClientProperties.class);

    public static <T> Future<T> run( RequestClientNode node) throws Throwable {
        if (!ResourceLocationUtils.check(node.getPath())) {
            throw new ResourceLocationFormatException("path format is error " + node.getPath());
        }
        RouteHystrixCommand<T> command = new RouteHystrixCommand<>(node, getSetter(node));
        return command.queue();
    }

    public static <T> T runSync(RequestClientNode node) throws Throwable {
        if (!ResourceLocationUtils.check(node.getPath())) {
            throw new ResourceLocationFormatException("path format is error " + node.getPath());
        }
        RouteHystrixCommand<T> command = new RouteHystrixCommand<>(node, getSetter(node));
        return command.execute();
    }

    private static Setter getSetter(RequestClientNode node) {
        CircuitBreakerProperties breaker = properties.getBreaker();
        return Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("kong-rpc")).andCommandKey(HystrixCommandKey.Factory.asKey(node.getAlias() + "-" + node.getPath() + "-" + node.getVersion()))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("kong-rpc-threadPool"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withCircuitBreakerEnabled(breaker.isEnable()).withCircuitBreakerRequestVolumeThreshold(breaker.getMax())
                        .withCircuitBreakerErrorThresholdPercentage(breaker.getRate()).withCircuitBreakerSleepWindowInMilliseconds(breaker.getSleep())
                        .withMetricsRollingStatisticalWindowInMilliseconds(breaker.getWindow()).withMetricsRollingStatisticalWindowBuckets(breaker.getBucket())
                        .withMetricsHealthSnapshotIntervalInMilliseconds(breaker.getInterval()).withFallbackEnabled(true).withExecutionTimeoutEnabled(false).withMetricsRollingPercentileEnabled(false)
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD).withRequestCacheEnabled(false))
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(properties.getThread().getCore()).withMaxQueueSize(properties.getThread().getQueue())
                        .withMaximumSize(properties.getThread().getCore()));
    }

//    private static Request buildRequest(String args, RequestClientNode clientNode) {
//        Request req = new Request();
//        req.setArgs(args);
//        req.setPath(clientNode.getPath());
//        req.setVersion(clientNode.getVersion());
//        req.setClientId(clientNode.getClientId());
//        req.setTransferMode(clientNode.getTransferMode());
//        return req;
//    }

}
