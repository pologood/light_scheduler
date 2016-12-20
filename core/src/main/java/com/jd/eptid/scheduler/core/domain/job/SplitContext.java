package com.jd.eptid.scheduler.core.domain.job;

/**
 * Created by classdan on 16-9-22.
 */
public class SplitContext<T extends Comparable> {
    private String jobName;
    private int splitTimes;
    private T previous;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public int getSplitTimes() {
        return splitTimes;
    }

    public void setSplitTimes(int splitTimes) {
        this.splitTimes = splitTimes;
    }

    public T getPrevious() {
        return previous;
    }

    public void setPrevious(T previous) {
        this.previous = previous;
    }
}
