package com.jd.eptid.scheduler.core.event;

import org.apache.zookeeper.ZooKeeper;

/**
 * Created by classdan on 16-12-23.
 */
public class ZkStateEvent extends AbstractEvent implements ZkEvent {
    private Code code;

    public ZkStateEvent(ZooKeeper zooKeeper, Code code) {
        super();
        setSource(zooKeeper);
        this.code = code;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public enum Code {
        CONNECTED, DISCONNECTED
    }
}
