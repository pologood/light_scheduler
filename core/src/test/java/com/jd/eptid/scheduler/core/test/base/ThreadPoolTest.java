package com.jd.eptid.scheduler.core.test.base;

import java.util.concurrent.*;

/**
 * Created by ClassDan on 2017/2/5.
 */
public class ThreadPoolTest {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(5, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "Cron-Job-Executor");
            }
        });
        scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);

        Future future = scheduledThreadPoolExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                System.out.println("Task running...");
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Task done.");
            }
        }, 0, 20, TimeUnit.SECONDS);

        TimeUnit.SECONDS.sleep(5);
        boolean cancel = future.cancel(false);
        System.out.println("Cancel result: " + cancel);
        Object o = future.get();
        System.out.println("Task result: " + o);

        scheduledThreadPoolExecutor.shutdownNow();
    }

}
