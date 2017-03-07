package com.jd.eptid.scheduler.core.event;

import com.jd.eptid.scheduler.core.domain.node.Client;

/**
 * Created by classdan on 17-1-12.
 */
public class ClientEvent extends AbstractEvent {
    private Code code;

    public ClientEvent(Client client, Code code) {
        setSource(client);
        this.code = code;
    }

    public Code getCode() {
        return code;
    }

    public enum Code {
        ADDED, REMOVED, DISABLED
    }
}
