package com.jd.eptid.scheduler.core.domain.node;

import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by classdan on 16-10-31.
 */
public abstract class Node {
    private static final char ROLE_SEPARATOR = '_';
    private static final char IP_PORT_SEPARATOR = ':';
    private static final Pattern idPattern = Pattern.compile("(.+)_(.+):(\\d+)");
    private static final Map<String, Class> classCache = new HashMap<String, Class>();
    private String ip;
    private int port;
    private long createTime;

    static {
        classCache.put("Client", Client.class);
        classCache.put("Server", Server.class);
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

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getIdentity() {
        StringBuilder idBuilder = new StringBuilder(this.getClass().getSimpleName());
        idBuilder.append(ROLE_SEPARATOR);
        idBuilder.append(ip).append(IP_PORT_SEPARATOR).append(port);
        return idBuilder.toString();
    }

    public static Node parse(String identity) {
        Assert.hasText(identity);

        Matcher matcher = idPattern.matcher(identity);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid node identity: " + identity);
        }

        String className = matcher.group(1);
        Node node = instanceNode(className);
        String ip = matcher.group(2);
        int port = Integer.parseInt(matcher.group(3));
        node.setIp(ip);
        node.setPort(port);
        return node;
    }

    private static Node instanceNode(String className) {
        Class nodeClass = classCache.get(className);
        Assert.notNull(nodeClass);
        try {
            return (Node) nodeClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (port != node.port) return false;
        return ip.equals(node.ip);
    }

    @Override
    public int hashCode() {
        int result = ip.hashCode();
        result = 31 * result + port;
        return result;
    }
}
