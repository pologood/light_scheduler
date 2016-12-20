package com.jd.eptid.scheduler.client.core;

import java.util.List;

/**
 * Created by classdan on 16-10-17.
 */
public class SplitResult<T> {
    private boolean isLast;
    private List<T> taskParams;

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }

    public List<T> getTaskParams() {
        return taskParams;
    }

    public void setTaskParams(List<T> taskParams) {
        this.taskParams = taskParams;
    }
}
