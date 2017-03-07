package com.jd.eptid.scheduler.core.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * An uniform shutdown hook to do some stop/destroy work when the system is shutdown.
 * The supported object should implement {@link com.jd.eptid.scheduler.core.common.LifeCycle} interface.
 * The order of invoking base on the return value of order method. The greater the first.
 * Created by classdan on 16-12-28.
 */
public class ShutdownHook {
    private final static ShutdownHook shutdownHook = new ShutdownHook();
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private TreeMap<Integer, List<LifeCycle>> lifeCycleObjects = new TreeMap<Integer, List<LifeCycle>>();

    private ShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new HookThread());
    }

    public static ShutdownHook getInstance() {
        return shutdownHook;
    }

    public synchronized void addLifeCycleObject(LifeCycle lifeCycle) {
        List<LifeCycle> lifeCycles = this.lifeCycleObjects.get(lifeCycle.order());
        if (lifeCycles == null) {
            lifeCycles = new ArrayList<LifeCycle>();
            lifeCycleObjects.put(lifeCycle.order(), lifeCycles);
        }
        lifeCycles.add(lifeCycle);
    }

    class HookThread extends Thread {
        @Override
        public void run() {
            logger.info("[ShutdownHook] Destroy all lifecycle objects...");

            NavigableMap<Integer, List<LifeCycle>> descendingMap = lifeCycleObjects.descendingMap();
            for (Map.Entry<Integer, List<LifeCycle>> entry : descendingMap.entrySet()) {
                List<LifeCycle> lifeCycles = entry.getValue();
                for (LifeCycle lifeCycle : lifeCycles) {
                    try {
                        lifeCycle.stop();
                    } catch (Throwable e) {
                        logger.error("Failed to stop lifeCycle object: {}.", lifeCycle, e);
                        // Ignore
                    }
                }
            }
        }
    }

}
