package com.jd.eptid.scheduler.core.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by classdan on 16-11-11.
 */
public abstract class AbstractTransport<T> implements Transport {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private List<TransportReadyListener<T>> transportReadyListeners = new CopyOnWriteArrayList<TransportReadyListener<T>>();

    protected void notifyReadyListeners(T t) {
        for (TransportReadyListener<T> transportReadyListener : transportReadyListeners) {
            try {
                transportReadyListener.onReady(t);
            } catch (Throwable e) {
                logger.error("Failed to notify listener: {}.", transportReadyListener, e);
            }
        }
    }

    @Override
    public void addReadyListener(TransportReadyListener listener) {
        transportReadyListeners.add(listener);
    }

}
