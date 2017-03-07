package com.jd.eptid.scheduler.server.chooser;

import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.core.event.ClientEvent;
import com.jd.eptid.scheduler.core.exception.ScheduleException;
import com.jd.eptid.scheduler.server.core.ClientManager;
import com.jd.eptid.scheduler.server.core.ServerContext;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by classdan on 17-1-17.
 */
public class SequentialTaskClientChooser implements TaskClientChooser {
    private String jobName;
    private ClientManager clientManager;
    private BlockingQueue<Client> clients = new LinkedBlockingQueue<Client>();

    public SequentialTaskClientChooser(String jobName) {
        this.jobName = jobName;
        clientManager = ServerContext.getInstance().getClientManager();
        ServerContext.getInstance().getEventBroadcaster().register(ClientEvent.class, this);
    }

    @Override
    public void init() {
        List<Client> schedulableClients = clientManager.getSchedulableClients(jobName);
        if (CollectionUtils.isEmpty(schedulableClients)) {
            throw new ScheduleException("No enough clients to run task.");
        }
        clients.addAll(schedulableClients);
    }

    @Override
    public Client choose() throws InterruptedException {
        return clients.take();
    }

    @Override
    public Client choose(long timeout, TimeUnit timeUnit) throws InterruptedException, TimeoutException {
        Client client = clients.poll(timeout, timeUnit);
        if (client == null) {
            throw new TimeoutException("Select task client timeout.");
        }
        return client;
    }

    @Override
    public void back(Client client) {
        if (client == null) {
            throw new IllegalArgumentException("Client should not be null.");
        }
        clients.add(client);
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
                clients.add(client);
                break;
            case DISABLED:
            case REMOVED:
                clients.remove(client);
                break;
            default:
                break;
        }
    }
}
