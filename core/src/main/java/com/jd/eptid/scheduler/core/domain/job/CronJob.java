package com.jd.eptid.scheduler.core.domain.job;

/**
 * Created by ClassDan on 2016/9/18.
 */
public class CronJob extends Job {
    private int periodicInterval;
    private boolean multipleExecution = false;

    public int getPeriodicInterval() {
        return periodicInterval;
    }

    public void setPeriodicInterval(int periodicInterval) {
        this.periodicInterval = periodicInterval;
    }

    public boolean allowMultipleExecution() {
        return multipleExecution;
    }

    public void setMultipleExecution(boolean multipleExecution) {
        this.multipleExecution = multipleExecution;
    }

    @Override
    public String toString() {
        return getName() + "[" + periodicInterval + "," + multipleExecution + "]";
    }
}
