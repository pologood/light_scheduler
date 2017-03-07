package com.jd.eptid.scheduler.core.listener;

import com.jd.eptid.scheduler.core.event.Event;

/**
 * Created by classdan on 16-12-21.
 */
public interface EventListener<T extends Event> {

    void onEvent(T event);

}
