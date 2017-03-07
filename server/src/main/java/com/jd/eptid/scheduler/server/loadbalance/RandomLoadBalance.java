package com.jd.eptid.scheduler.server.loadbalance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by classdan on 16-9-29.
 */
public class RandomLoadBalance<T> implements LoadBalance<T> {

    @Override
    public T select(List<T> shards, String seed) {
        return shards.get(ThreadLocalRandom.current().nextInt(shards.size()));
    }

}
