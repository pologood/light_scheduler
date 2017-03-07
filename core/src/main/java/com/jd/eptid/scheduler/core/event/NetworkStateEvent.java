package com.jd.eptid.scheduler.core.event;

/**
 * Created by classdan on 16-12-20.
 */
public class NetworkStateEvent extends AbstractEvent {
    private Code code;

    public NetworkStateEvent(Object source, Code code) {
        super();
        this.code = code;
        setSource(source);
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public enum Code {
        READY, UNAVAILABLE
    }

}
