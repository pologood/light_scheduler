package com.jd.eptid.scheduler.server.loadbalance;

import java.util.List;

/**
 * Created by classdan on 16-9-29.
 */
public interface LoadBalance<T> {

    T select(List<T> shards, String seed);

}
