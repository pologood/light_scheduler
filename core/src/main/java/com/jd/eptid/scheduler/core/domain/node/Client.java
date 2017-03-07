package com.jd.eptid.scheduler.core.domain.node;

import org.apache.commons.collections.CollectionUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by classdan on 16-9-9.
 */
public class Client extends Node {
    private Set<String> supportJobs = new HashSet<String>();

    public Client() {

    }

    public Client(String ip, int port) {
        this(ip, port, null);
    }

    public Client(String ip, int port, Set<String> supportJobs) {
        setIp(ip);
        setPort(port);
        setCreateTime(System.currentTimeMillis());

        if (!CollectionUtils.isEmpty(supportJobs)) {
            this.supportJobs = supportJobs;
        }
    }

    public Set<String> getSupportJobs() {
        return supportJobs;
    }

    public void setSupportJobs(Set<String> supportJobs) {
        this.supportJobs = supportJobs;
    }

    @Override
    public Object clone() {
        Client client = null;
        try {
            client = (Client) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        client.supportJobs = new HashSet<String>(supportJobs);
        return client;
    }

    @Override
    public String toString() {
        return "Client[" + getIp() + ":" + getPort() + "]";
    }
}
