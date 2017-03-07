package com.jd.eptid.scheduler.core.event;


import com.jd.eptid.scheduler.core.listener.EventListener;

import java.util.Collection;

/**
 * Created by classdan on 16-12-21.
 */
public interface EventBroadcaster {

    void register(Class<? extends Event> eventType, EventListener listener);

    void unregister(Class<? extends Event> eventType, EventListener listener);

    void publish(Event event);

    void broadcast(EventListener listener, Event event);

    void broadcast(Collection<? extends EventListener> listeners, Event event);

}
