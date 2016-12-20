package com.jd.eptid.scheduler.core.network;

/**
 * Created by classdan on 16-10-31.
 */
public interface Transport {

    void start();

    void shutdown();

    boolean isAlive();

    void addReadyListener(TransportReadyListener listener);

}
