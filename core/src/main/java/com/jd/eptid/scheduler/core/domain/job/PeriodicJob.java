package com.jd.eptid.scheduler.core.domain.job;

/**
 * Created by ClassDan on 2016/9/18.
 */
public class PeriodicJob extends Job {
    private int periodicInterval;
    private boolean allowConcurrent = false;

    public int getPeriodicInterval() {
        return periodicInterval;
    }

    public void setPeriodicInterval(int periodicInterval) {
        this.periodicInterval = periodicInterval;
    }

    public boolean isAllowConcurrent() {
        return allowConcurrent;
    }

    public void setAllowConcurrent(boolean allowConcurrent) {
        this.allowConcurrent = allowConcurrent;
    }

    @Override
    public String toString() {
        return getName() + "[" + periodicInterval + "," + allowConcurrent + "]";
    }
}
