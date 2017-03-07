package com.jd.eptid.scheduler.core.event;

import java.util.List;

/**
 * Created by classdan on 16-12-21.
 */
public class ZChildrenEvent extends AbstractEvent implements ZkEvent {
    private String path;
    private List<String> children;

    public ZChildrenEvent(String path, List<String> children) {
        super();
        this.path = path;
        this.children = children;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }
}
