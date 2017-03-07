package com.jd.eptid.scheduler.core.event;

/**
 * Created by classdan on 16-12-20.
 */
public class ZNodeEvent extends AbstractEvent implements ZkEvent {
    private String path;
    private Code code;

    public ZNodeEvent(String path, Code code) {
        super();
        this.path = path;
        this.code = code;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public enum Code {
        ADD,
        REMOVED,
        CHANGED
    }

}
