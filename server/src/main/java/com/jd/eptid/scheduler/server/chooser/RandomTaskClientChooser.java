package com.jd.eptid.scheduler.server.chooser;

import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.core.event.ClientEvent;
import com.jd.eptid.scheduler.core.exception.ScheduleException;
import com.jd.eptid.scheduler.server.core.ServerContext;
import com.jd.eptid.scheduler.server.loadbalance.LoadBalance;
import com.jd.eptid.scheduler.server.loadbalance.RandomLoadBalance;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A task client chooser base on random algorithm.
 * Created by classdan on 17-1-18.
 */
public class RandomTaskClientChooser implements TaskClientChooser {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private String jobName;
    private Set<Client> scheduableClients = new HashSet<Client>();
    private Set<Client> availableClients = new HashSet<Client>();
    private LoadBalance<Client> loadBalance;

    public RandomTaskClientChooser(String jobName) {
        this.jobName = jobName;
        loadBalance = new RandomLoadBalance<Client>();
        ServerContext.getInstance().getEventBroadcaster().register(ClientEvent.class, this);
    }

    @Override
    public void init() {
        List<Client> schedulableClients = ServerContext.getInstance().getClientManager().getSchedulableClients(jobName);
        if (CollectionUtils.isEmpty(schedulableClients)) {
            throw new ScheduleException("Not enough clients to run task.");
        }
        this.scheduableClients.addAll(schedulableClients);
        this.availableClients.addAll(schedulableClients);
    }

    @Override
    public synchronized Client choose() throws InterruptedException {
        while (availableClients.isEmpty()) {
            if (scheduableClients.isEmpty()) {
                throw new ScheduleException("Not enough clients to run task.");
            }

            wait();
        }

        logger.info("availableClients: {}.", availableClients);
        Client client = loadBalance.select(new ArrayList<Client>(availableClients), null);
        availableClients.remove(client);
        return client;
    }

    @Override
    public synchronized Client choose(long timeout, TimeUnit timeUnit) throws InterruptedException, TimeoutException {
        if (availableClients.isEmpty()) {
            wait(timeUnit.toMillis(timeout));
        }
        if (availableClients.isEmpty()) {
            throw new TimeoutException("Wait for a available client timeout.");
        }

        Client client = loadBalance.select(new ArrayList<Client>(availableClients), null);
        availableClients.remove(client);
        return client;
    }

    @Override
    public synchronized void back(Client client) {
        if (scheduableClients.contains(client)) {
            availableClients.add(client);
            notifyAll();
        }
    }

    @Override
    public void destroy() {
        ServerContext.getInstance().getEventBroadcaster().unregister(ClientEvent.class, this);
    }

    @Override
    public void onEvent(ClientEvent event) {
        logger.info("ClientEvent: {}, {}.", event.getCode(), event.source());
        Client client = (Client) event.source();
        switch (event.getCode()) {
            case ADDED:
                if (isAvailableClient(client)) {
                    addAvailableClient(client);
                }
                break;
            case DISABLED:
            case REMOVED:
                removeAvailableClient(client);
                break;
            default:
                break;
        }
    }

    private boolean isAvailableClient(Client client) {
        Set<String> supportJobs = client.getSupportJobs();
        if (CollectionUtils.isEmpty(supportJobs)) {
            return false;
        }
        return supportJobs.contains(jobName);
    }

    private synchronized void addAvailableClient(Client client) {
        availableClients.add(client);
        scheduableClients.add(client);
        notifyAll();
    }

    private synchronized void removeAvailableClient(Client client) {
        availableClients.remove(client);
        scheduableClients.remove(client);
        logger.info("Remove client: {}. availableClients:{}.", client, availableClients);

        if (scheduableClients.isEmpty()) {
            notifyAll();
        }
    }

}
