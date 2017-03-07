package com.jd.eptid.scheduler.server.chooser;

import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.core.event.ClientEvent;
import com.jd.eptid.scheduler.core.exception.ScheduleException;
import com.jd.eptid.scheduler.server.core.ClientManager;
import com.jd.eptid.scheduler.server.core.ServerContext;
import com.jd.eptid.scheduler.server.loadbalance.LoadBalance;
import com.jd.eptid.scheduler.server.loadbalance.RandomLoadBalance;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A job client chooser base on random algorithm.
 * Created by classdan on 17-1-17.
 */
public class RandomJobClientChooser implements JobClientChooser {
    private String jobName;
    private ClientManager clientManager;
    private LoadBalance<Client> loadBalance;
    private Set<Client> availableClients = new HashSet<Client>();
    private final Queue<Client> usedClients = new LinkedList<Client>();
    private int latestUsedClientNum = 3;

    public RandomJobClientChooser(String jobName) {
        this.jobName = jobName;
        clientManager = ServerContext.getInstance().getClientManager();
        loadBalance = new RandomLoadBalance<Client>();
        ServerContext.getInstance().getEventBroadcaster().register(ClientEvent.class, this);
    }

    @Override
    public void init() {
        List<Client> schedulableClients = clientManager.getSchedulableClients(jobName);
        if (CollectionUtils.isEmpty(schedulableClients)) {
            throw new ScheduleException("Not enough clients for job: " + jobName);
        }
        availableClients.addAll(schedulableClients);
    }

    @Override
    public Client choose() throws InterruptedException {
        synchronized (availableClients) {
            if (availableClients.isEmpty()) {
                availableClients.wait();
            }
        }

        return doChoose();
    }

    private Client doChoose() {
        List<Client> candidateClients = new LinkedList<Client>();
        for (Client client : availableClients) {
            candidateClients.add((Client) client.clone());
        }

        candidateClients.removeAll(usedClients);
        Client jobClient = null;
        if (candidateClients.isEmpty()) {
            jobClient = loadBalance.select(new ArrayList<Client>(availableClients), null);
        } else {
            jobClient = loadBalance.select(candidateClients, null);
        }
        recordUsedClient(jobClient);
        return jobClient;
    }

    private void recordUsedClient(Client aClient) {
        if (usedClients.size() >= latestUsedClientNum) {
            usedClients.poll();
        }
        usedClients.offer(aClient);
    }

    @Override
    public Client choose(long timeout, TimeUnit timeUnit) throws InterruptedException, TimeoutException {
        synchronized (availableClients) {
            if (availableClients.isEmpty()) {
                availableClients.wait(timeUnit.toMillis(timeout));
            }
        }
        if (availableClients.isEmpty()) {
            throw new TimeoutException("Wait for a available client timeout.");
        }

        return doChoose();
    }

    @Override
    public void back(Client client) {
        //not implement
    }

    @Override
    public void destroy() {
        ServerContext.getInstance().getEventBroadcaster().unregister(ClientEvent.class, this);
    }

    @Override
    public void onEvent(ClientEvent event) {
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

    private void addAvailableClient(Client client) {
        synchronized (availableClients) {
            availableClients.add(client);
            availableClients.notifyAll();
        }
    }

    private void removeAvailableClient(Client client) {
        synchronized (availableClients) {
            availableClients.remove(client);
        }
    }

}
