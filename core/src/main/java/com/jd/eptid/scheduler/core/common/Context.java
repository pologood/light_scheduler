package com.jd.eptid.scheduler.core.common;

import com.jd.eptid.scheduler.core.event.AsyncEventBroadcaster;
import com.jd.eptid.scheduler.core.event.EventBroadcaster;
import com.jd.eptid.scheduler.core.zk.ZookeeperEndpoint;

/**
 * Created by classdan on 17-1-5.
 */
public abstract class Context {
    private EventBroadcaster eventBroadcaster;
    private ZookeeperEndpoint zookeeperEndpoint;

    public Context() {
        eventBroadcaster = new AsyncEventBroadcaster();
        zookeeperEndpoint = new ZookeeperEndpoint(eventBroadcaster);
    }

    public EventBroadcaster getEventBroadcaster() {
        return eventBroadcaster;
    }

    public ZookeeperEndpoint getZookeeperEndpoint() {
        return zookeeperEndpoint;
    }
}
