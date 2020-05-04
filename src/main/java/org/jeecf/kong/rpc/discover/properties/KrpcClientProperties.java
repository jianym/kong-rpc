package org.jeecf.kong.rpc.discover.properties;

import java.util.List;

import org.jeecf.kong.rpc.center.ZkProperties;
import org.jeecf.kong.rpc.protocol.properties.SocketProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 客户端配置文件
 * 
 * @author jianyiming
 *
 */
@Data
@Component
@ConfigurationProperties(prefix = "krpc.client")
public class KrpcClientProperties {
    /**
     * 名称
     */
    private String name = "";
    /**
     * 路由
     */
    private String route = "loop";
    /**
     * 线程
     */
    private ThreadProperties thread = new ThreadProperties();
    /**
     * 通信
     */
    private SocketProperties socket = new SocketProperties();
    /**
     * 断路器
     */
    private CircuitBreakerProperties breaker = new CircuitBreakerProperties();
    /**
     * zk 配置
     */
    private ZkProperties zookeeper;
    /**
     * 其他服务器
     */
    private List<KrpcProperties> alias;;

    public class ThreadProperties {
        /**
         * 核心线程数
         */
        private int core = 10;
        /**
         * 队列大小
         */
        private int queue = -1;

        public int getCore() {
            return core;
        }

        public void setCore(int core) {
            this.core = core;
        }

        public int getQueue() {
            return queue;
        }

        public void setQueue(int queue) {
            this.queue = queue;
        }

    }

    public class CircuitBreakerProperties {
        /**
         * 是否接入断路器
         */
        private boolean enable = false;
        /**
         * 断路失败阀值
         */
        private int max = 20;
        /**
         * 断路失败比率
         */
        private int rate = 50;
        /**
         * 检测窗口
         */
        private int window = 5 * 60 * 1000;
        /**
         * 断路器打开后，间隔多久进入半打开
         */
        private int sleep = 30 * 60 * 1000;
        /**
         * 多久检测一次
         */
        private int interval = 3 * 1000;
        /**
         * 桶，检测窗口划分为多少桶
         */
        private int bucket = 20;

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
        }

        public int getRate() {
            return rate;
        }

        public void setRate(int rate) {
            this.rate = rate;
        }

        public int getWindow() {
            return window;
        }

        public void setWindow(int window) {
            this.window = window;
        }

        public int getSleep() {
            return sleep;
        }

        public void setSleep(int sleep) {
            this.sleep = sleep;
        }

        public int getInterval() {
            return interval;
        }

        public void setInterval(int interval) {
            this.interval = interval;
        }

        public int getBucket() {
            return bucket;
        }

        public void setBucket(int bucket) {
            this.bucket = bucket;
        }

    }

}
