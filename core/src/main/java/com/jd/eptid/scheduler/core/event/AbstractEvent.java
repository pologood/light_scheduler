package com.jd.eptid.scheduler.core.event;

/**
 * Created by classdan on 16-12-20.
 */
public abstract class AbstractEvent implements Event {
    private Object source;
    private long timestamp;

    public AbstractEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public Object source() {
        return source;
    }

    @Override
    public long time() {
        return timestamp;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
