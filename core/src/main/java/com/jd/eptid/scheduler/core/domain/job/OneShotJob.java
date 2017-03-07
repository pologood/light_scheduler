package com.jd.eptid.scheduler.core.domain.job;

/**
 * Created by ClassDan on 2016/9/18.
 */
public class OneShotJob extends Job {

    @Override
    public String toString() {
        return getName() + "[name: " + getName() + ", startTime: " + getStartTime() + "]";
    }

}
