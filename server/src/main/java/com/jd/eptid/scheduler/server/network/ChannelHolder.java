package com.jd.eptid.scheduler.server.network;

import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.core.utils.NetworkUtils;
import io.netty.channel.Channel;
import org.springframework.util.Assert;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by ClassDan on 2016/9/15.
 */
public class ChannelHolder {
    private static final ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<String, Channel>();

    public static void addChannel(Channel channel) {
        channels.put(generateChannelId(channel), channel);
    }

    public static void removeChannel(Channel channel) {
        channels.remove(generateChannelId(channel));
    }

    public static Channel getChannel(String channelId) {
        return channels.get(channelId);
    }

    public static String generateChannelId(Channel channel) {
        Assert.notNull(channel);

        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        String remoteIp = NetworkUtils.getIpAddress(socketAddress);
        int port = NetworkUtils.getPort(socketAddress);
        return generateId(remoteIp, port);
    }

    private static String generateId(String ip, int port) {
        return ip + ":" + port;
    }

    public static Channel findChannel(Client client) {
        return channels.get(generateId(client.getIp(), client.getPort()));
    }

}
