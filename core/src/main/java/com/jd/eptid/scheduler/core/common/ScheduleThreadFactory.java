package com.jd.eptid.scheduler.core.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by classdan on 17-1-20.
 */
public class ScheduleThreadFactory implements ThreadFactory {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private boolean isGroup;
    private String namePrefix;
    private final AtomicInteger threadNo = new AtomicInteger(1);

    public ScheduleThreadFactory(String namePrefix) {
        this(namePrefix, true);
    }

    public ScheduleThreadFactory(String namePrefix, boolean isGroup) {
        this.namePrefix = namePrefix;
        this.isGroup = isGroup;
    }

    @Override
    public Thread newThread(Runnable r) {
        String threadName = isGroup ? (namePrefix + threadNo.getAndIncrement()) : namePrefix;
        Thread t = new Thread(r, threadName);
        t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                logger.error("Uncaught exception!", e);
            }
        });
        return t;
    }
}
