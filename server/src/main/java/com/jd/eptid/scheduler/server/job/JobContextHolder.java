package com.jd.eptid.scheduler.server.job;

/**
 * Created by classdan on 16-9-22.
 */
public class JobContextHolder {
    private static final ThreadLocal<JobContext> context = new ThreadLocal<JobContext>();

    public static void setContext(JobContext jobContext) {
        context.set(jobContext);
    }

    public static JobContext getContext() {
        return context.get();
    }

    public static void remove() {
        context.remove();
    }

}
