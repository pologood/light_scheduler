package com.jd.eptid.scheduler.server.network;


import com.jd.eptid.scheduler.core.domain.message.Message;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by classdan on 16-9-22.
 */
public class ResponseFuturePool {
    private static ConcurrentMap<String, ResponseFuture<Message>> responseFutures = new ConcurrentHashMap<String, ResponseFuture<Message>>();
    private static ScheduledExecutorService clearExecutorService = Executors.newSingleThreadScheduledExecutor();

    static {
        clearExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (responseFutures.size() > 0) {
                    Iterator<Map.Entry<String, ResponseFuture<Message>>> iterator = responseFutures.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, ResponseFuture<Message>> entry = iterator.next();
                        ResponseFuture<Message> responseFuture = entry.getValue();
                        if (responseFuture.isTimeout()) {
                            iterator.remove();
                        }
                    }
                }
            }
        }, 10, 1, TimeUnit.SECONDS);
    }

    public static ResponseFuture<Message> getResponseFuture(String messageId) {
        return responseFutures.get(messageId);
    }

    public static void put(String messageId, ResponseFuture<Message> future) {
        responseFutures.put(messageId, future);
    }
}
