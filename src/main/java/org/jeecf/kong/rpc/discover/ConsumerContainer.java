package org.jeecf.kong.rpc.discover;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.jeecf.kong.rpc.protocol.NettyClient;

/**
 * 客户端容器 用于获取服务节点
 * 
 * @author jianyiming
 *
 */
public class ConsumerContainer {

    private volatile static ConsumerContainer consumerContainer = null;

    private ConsumerContainer() {
    }

    public static ConsumerContainer getInstance() {
        if (consumerContainer != null)
            return consumerContainer;
        synchronized (ConsumerContainer.class) {
            if (consumerContainer != null)
                return consumerContainer;
            return consumerContainer = new ConsumerContainer();
        }
    }

    private Map<String, LruLinkedMap> aliasListMap = new ConcurrentHashMap<>();

    public void put(String alias, String key, ServerNode value) {
        LruLinkedMap linkedMap = aliasListMap.get(alias);
        if (linkedMap == null) {
            linkedMap = new LruLinkedMap();
            aliasListMap.put(alias, linkedMap);
        }
        linkedMap.put(key, value);
    }

    public void remove(String alias, String key) {
        LruLinkedMap linkedMap = aliasListMap.get(alias);
        if (linkedMap != null && linkedMap.size() > 0)
            linkedMap.remove(key);
    }

    public ServerNode get(String alias, String key) {
        LruLinkedMap linkedMap = aliasListMap.get(alias);
        if (linkedMap != null && linkedMap.size() > 0)
            return linkedMap.get(key);
        return null;
    }

    public int size(String alias) {
        LruLinkedMap linkedMap = aliasListMap.get(alias);
        if (linkedMap != null && linkedMap.size() > 0)
            return linkedMap.size();
        return -1;
    }

    public ServerNode getIndex(String alias, Integer index) {
        LruLinkedMap linkedMap = aliasListMap.get(alias);
        if (linkedMap != null && linkedMap.size() > 0)
            return linkedMap.getIndex(index);
        return null;
    }

    /**
     * 服务节点
     * 
     * @author jianyiming
     *
     */
    public class ServerNode {

        public static final byte STATE_INIT = 0;

        public static final byte STATE_OPEN = 1;

        private volatile NettyClient nettyClient;

        private volatile ServerNode next;

        private volatile String ip;

        private int port;

        private int timeout;

        private int low;

        private int height;

        private byte state;

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public NettyClient getNettyClient() {
            return nettyClient;
        }

        public void setNettyClient(NettyClient nettyClient) {
            this.nettyClient = nettyClient;
        }

        public byte getState() {
            return state;
        }

        public void setState(byte state) {
            this.state = state;
        }

        public ServerNode getNext() {
            return next;
        }

        public void setNext(ServerNode next) {
            this.next = next;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public int getLow() {
            return low;
        }

        public void setLow(int low) {
            this.low = low;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

    }

    /**
     * 服务节点 链表
     * 
     * @author jianyiming
     *
     */
    public class LruLinkedMap {

        private volatile ServerNode head;

        private volatile ServerNode tail;

        private Map<String, ServerNode> nodeMap = new ConcurrentHashMap<>();

        private AtomicInteger state = new AtomicInteger(0);

        private volatile boolean readLock = false;

        private volatile int writeState = 0;

        private ReentrantLock writeLock = new ReentrantLock(true);

        public ServerNode getHead() {
            return head;
        }

        public void setHead(ServerNode head) {
            this.head = head;
        }

        public ServerNode getTail() {
            return tail;
        }

        public void setTail(ServerNode tail) {
            this.tail = tail;
        }

        public boolean put(String key, ServerNode value) {
            try {
                writeLock.lock();
                writeState++;
                ServerNode node = nodeMap.get(key);
                if (node != null)
                    return false;
                try {
                    while (true) {
                        if (state.compareAndSet(0, 1)) {
                            readLock = false;
                            break;
                        }
                    }
                    node = nodeMap.get(key);
                    if (node != null)
                        return false;
                    if (head == null) {
                        head = value;
                        tail = value;
                    } else {
                        tail.setNext(value);
                        tail = value;
                    }
                    nodeMap.put(key, value);
                    return true;
                } finally {
                    state.decrementAndGet();
                }
            } finally {
                if (writeLock.getHoldCount() > 0) {
                    writeState--;
                }
                writeLock.unlock();
            }
        }

        public void remove(String key) {
            ServerNode node = nodeMap.get(key);
            if (node == null) {
                return;
            }
            try {
                writeLock.lock();
                writeState++;
                while (true) {
                    if (state.compareAndSet(0, 1)) {
                        readLock = false;
                        break;
                    }
                }
                try {
                    node = nodeMap.get(key);
                    if (node == null)
                        return;
                    ServerNode nextNode = head;
                    ServerNode preNode = null;
                    while (nextNode != null) {
                        if (nextNode == node) {
                            if (nextNode == head)
                                head = head.next;
                            else
                                preNode.next = nextNode.next;
                            nextNode = null;
                            if (head == null || head.next == null) {
                                tail = head;
                            }
                            break;
                        }
                        preNode = nextNode;
                        nextNode = nextNode.next;
                    }
                    nodeMap.remove(key);
                } finally {
                    state.decrementAndGet();
                }
            } finally {
                if (writeLock.getHoldCount() > 0) {
                    writeState--;
                }
                writeLock.unlock();
            }
        }

        public ServerNode get(String key) {
            return nodeMap.get(key);
        }

        public ServerNode getIndex(int index) {
            while (true) {
                // 并发读
                if (readLock && writeState == 0) {
                    state.incrementAndGet();
                    break;
                }
                // 临界区间执行时间段，写低频 自旋，优先写
                if (writeState > 0) {
                    continue;
                }
                // 争读锁
                if (state.compareAndSet(0, 1)) {
                    readLock = true;
                    break;
                }
            }
            try {
                ServerNode result = null;
                ServerNode nextNode = head;
                int i = 0;
                while (nextNode != null) {
                    if (i == index) {
                        result = nextNode;
                        break;
                    }
                    nextNode = nextNode.next;
                    i++;
                }
                if (state.get() == 1) {
                    readLock = false;
                }
                return result;
            } finally {
                state.decrementAndGet();
            }
        }

        public int size() {
            return nodeMap.size();
        }

    }

}
