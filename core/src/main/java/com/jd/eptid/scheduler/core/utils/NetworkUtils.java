package com.jd.eptid.scheduler.core.utils;

import io.netty.channel.Channel;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ClassDan on 2016/9/15.
 */
public class NetworkUtils {

    public static Set<InetAddress> getLocalAddresses() {
        Set<InetAddress> addrs = new HashSet<InetAddress>();
        Enumeration<NetworkInterface> ns = null;
        try {
            ns = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            // ignored...
        }
        while (ns != null && ns.hasMoreElements()) {
            NetworkInterface n = ns.nextElement();
            Enumeration<InetAddress> is = n.getInetAddresses();
            while (is.hasMoreElements()) {
                InetAddress i = is.nextElement();
                if (!i.isLoopbackAddress() && !i.isLinkLocalAddress() && !i.isMulticastAddress()
                        && !isSpecialIp(i.getHostAddress())) addrs.add(i);
            }
        }
        return addrs;
    }

    public static String[] getLocalIpAddresses() {
        Set<InetAddress> addrs = getLocalAddresses();
        String[] ret = new String[addrs.size()];
        int i = 0;
        for (InetAddress addr : addrs) {
            ret[i++] = addr.getHostAddress();
        }
        return ret;
    }

    private static boolean isSpecialIp(String ip) {
        if (ip.contains(":")) return true;
        if (ip.startsWith("127.")) return true;
        if (ip.startsWith("169.254.")) return true;
        if (ip.equals("255.255.255.255")) return true;
        return false;
    }

    public static String getIpAddress(InetSocketAddress inetSocketAddress) {
        if (inetSocketAddress == null) {
            return null;
        }

        InetAddress inetAddress = inetSocketAddress.getAddress();
        return inetAddress != null ? inetAddress.getHostAddress() : inetSocketAddress.getHostName();
    }

    public static int getPort(InetSocketAddress inetSocketAddress) {
        Assert.notNull(inetSocketAddress);

        return inetSocketAddress.getPort();
    }

    public static Pair<String, Integer> getIpAndPort(Channel channel) {
        InetSocketAddress serverSocketAddress = (InetSocketAddress) channel.localAddress();
        String ip = NetworkUtils.getIpAddress(serverSocketAddress);
        int port = NetworkUtils.getPort(serverSocketAddress);
        return Pair.of(ip, port);
    }

}
