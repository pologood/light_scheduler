package com.jd.eptid.scheduler.core.network;

import com.jd.eptid.scheduler.core.common.LifeCycle;
import com.jd.eptid.scheduler.core.listener.NetworkEventListener;

/**
 * Created by classdan on 16-10-31.
 */
public interface Transport extends LifeCycle {

    boolean isAlive();

}
