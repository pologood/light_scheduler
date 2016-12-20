package com.jd.eptid.scheduler.core.domain.node;

/**
 * Created by classdan on 16-10-31.
 */
public class Server extends Node {
    private ServerRole role = ServerRole.BACKUP;

    public ServerRole getRole() {
        return role;
    }

    public void setRole(ServerRole role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "Server[" + getIp() + ":" + getPort() + "]";
    }
}
