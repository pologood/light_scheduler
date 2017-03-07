package com.jd.eptid.scheduler.client.master;

import com.jd.eptid.scheduler.core.listener.MasterChangeListener;

/**
 * Created by classdan on 16-11-14.
 */
public interface MasterWatcher {

    void addListener(MasterChangeListener listener);

}
