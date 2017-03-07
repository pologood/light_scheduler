package com.jd.eptid.scheduler.core.event;

import com.google.common.base.Throwables;
import com.jd.eptid.scheduler.core.common.LifeCycle;
import com.jd.eptid.scheduler.core.common.ShutdownHook;
import com.jd.eptid.scheduler.core.listener.EventListener;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by classdan on 16-12-21.
 */
public class AsyncEventBroadcaster implements EventBroadcaster, LifeCycle {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>();
    private Map<Class<? extends Event>, Set<EventListener>> concernedListeners = new HashMap<>();
    private Thread dispatchThread = null;
    private volatile boolean isRunning = true;

    public AsyncEventBroadcaster() {
        start();
        ShutdownHook.getInstance().addLifeCycleObject(this);
    }

    @Override
    public synchronized void register(Class<? extends Event> eventType, EventListener listener) {
        Set<EventListener> eventListeners = concernedListeners.get(eventType);
        if (eventListeners == null) {
            eventListeners = new HashSet<EventListener>();
            concernedListeners.put(eventType, eventListeners);
        }
        eventListeners.add(listener);
    }

    @Override
    public synchronized void unregister(Class<? extends Event> eventType, EventListener listener) {
        Set<EventListener> eventListeners = concernedListeners.get(eventType);
        if (CollectionUtils.isNotEmpty(eventListeners)) {
            eventListeners.remove(listener);
        }
    }

    @Override
    public void publish(Event event) {
        try {
            eventQueue.put(event);
        } catch (InterruptedException e) {
            logger.error("Failed to publish event: {}.", event);
            Throwables.propagate(e);
        }
    }

    @Override
    public void broadcast(EventListener listener, Event event) {
        executorService.submit(new NotifyTask(listener, event));
    }

    @Override
    public void broadcast(Collection<? extends EventListener> listeners, Event event) {
        if (CollectionUtils.isEmpty(listeners)) {
            return;
        }

        for (EventListener listener : listeners) {
            this.broadcast(listener, event);
        }
    }

    @Override
    public void start() {
        if (dispatchThread != null) {
            return;
        }
        startDispatchThread();
    }

    private void startDispatchThread() {
        Thread dispatchThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    Event event = null;
                    try {
                        event = eventQueue.take();
                        Set<EventListener> eventListeners = concernedListeners.get(event.getClass());
                        if (eventListeners == null) {
                            continue;
                        }
                        broadcast(eventListeners, event);
                    } catch (InterruptedException e) {
                        if (event != null) {
                            logger.error("Failed to dispatch event: {}, it probably to be lost.", event.source());
                        }
                    }
                }
            }
        }, "EventDispatcher");
        dispatchThread.setDaemon(true);
        dispatchThread.start();
        this.dispatchThread = dispatchThread;
    }

    @Override
    public void stop() {
        logger.info("Stop event broadcaster...");
        isRunning = false;
        dispatchThread.interrupt();
        dispatchThread = null;
        executorService.shutdown();
    }

    @Override
    public int order() {
        return 0;
    }

    class NotifyTask implements Runnable {
        private EventListener listener;
        private Event event;

        public NotifyTask(EventListener listener, Event event) {
            this.listener = listener;
            this.event = event;
        }

        @Override
        public void run() {
            try {
                listener.onEvent(event);
            } catch (Throwable e) {
                logger.error("Failed to notify listener. Listener: {}. Event: {}.", listener, event, e);
            }
        }

    }
}
