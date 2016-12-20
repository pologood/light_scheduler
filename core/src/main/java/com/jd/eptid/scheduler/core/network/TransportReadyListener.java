package com.jd.eptid.scheduler.core.network;

/**
 * Created by classdan on 16-11-11.
 */
public interface TransportReadyListener<T> {

    void onReady(T t);

}
