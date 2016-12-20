package com.jd.eptid.scheduler.core.domain.node;

import java.util.Set;

/**
 * Created by classdan on 16-9-9.
 */
public class Client extends Node {
    private Set<String> supportJobs;

    public Client() {

    }

    public Client(String ip, int port, Set<String> supportJobs) {
        setIp(ip);
        setPort(port);
        setCreateTime(System.currentTimeMillis());

        this.supportJobs = supportJobs;
    }

    public Set<String> getSupportJobs() {
        return supportJobs;
    }

    public void setSupportJobs(Set<String> supportJobs) {
        this.supportJobs = supportJobs;
    }

    @Override
    public String toString() {
        return "Client[" + getIp() + ":" + getPort() + "]";
    }
}
