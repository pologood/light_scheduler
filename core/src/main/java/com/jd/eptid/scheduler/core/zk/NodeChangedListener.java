package com.jd.eptid.scheduler.core.zk;

import com.jd.eptid.scheduler.core.domain.Event;

/**
 * Created by classdan on 16-11-8.
 */
public interface NodeChangedListener {

    void onChange(String path, Event event);

}
