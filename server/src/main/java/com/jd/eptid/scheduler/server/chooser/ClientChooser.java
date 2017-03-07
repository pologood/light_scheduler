package com.jd.eptid.scheduler.server.chooser;


import com.jd.eptid.scheduler.core.domain.node.Client;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Choose a client to do something. e.g. job splitting, task executing.
 * Created by classdan on 16-9-22.
 */
public interface ClientChooser {

    /**
     * Initialize the chooser.
     */
    void init();

    /**
     * Choose a client base on a strategy.
     *
     * @return a client
     * @throws InterruptedException if the current thread was interrupted while waiting
     */
    Client choose() throws InterruptedException;

    /**
     * Waits if necessary for at most the given time for choosing a client base on a strategy.
     *
     * @param timeout  the maximum time to wait
     * @param timeUnit time unit
     * @return a client
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws TimeoutException     if the wait timed out
     */
    Client choose(long timeout, TimeUnit timeUnit) throws InterruptedException, TimeoutException;

    /**
     * Return back a client
     *
     * @param client a client
     */
    void back(Client client);

    /**
     * Release all dependent resources, or do some post-process.
     */
    void destroy();

}
