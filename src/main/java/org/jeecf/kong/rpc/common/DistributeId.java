package org.jeecf.kong.rpc.common;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.util.StringUtils;

/**
 * 分布式id
 * 
 * @author jianyiming
 *
 */
public class DistributeId {

    private static String ip = null;

    private static long time = 0;

    private static AtomicInteger incr = new AtomicInteger(0);

    static {
        try {
            ip = getLocalHostLANAddress().getHostAddress();
            ip = StringUtils.replace(ip, ".", "");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static String getId() {
        if (ip != null) {
            long ms = System.currentTimeMillis();
            int temp = 0;
            if (ms == time) {
                temp = incr.incrementAndGet();
            }
            time = ms;
            return ip + "-" + ms + "-" + temp;
        }
        return null;
    }

    public static InetAddress getLocalHostLANAddress() throws UnknownHostException {
        try {
            InetAddress candidateAddress = null;
            // 遍历所有的网络接口
            for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // 在所有的接口下再遍历IP
                for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            // site-local类型的地址未被发现，先记录候选地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            // 如果没有发现 non-loopback地址.只能用最次选的方案
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress;
        } catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + e);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }

}
