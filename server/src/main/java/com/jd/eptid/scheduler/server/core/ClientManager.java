package com.jd.eptid.scheduler.server.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.core.event.ClientEvent;
import io.netty.util.internal.ConcurrentSet;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by classdan on 16-9-9.
 */
@Component
public class ClientManager {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Set<Client> clients = new ConcurrentSet<Client>();
    private ConcurrentMap<String, Set<Client>> schedulableClients = new ConcurrentHashMap<String, Set<Client>>();

    public void register(String ip, int port, Set<String> supportedJobNames) {
        Assert.notEmpty(supportedJobNames, "supportedJobNames should not be empty.");
        Assert.hasText(ip, "ip should not be empty.");

        Client client = new Client(ip, port, supportedJobNames);
        clients.add(client);
        addSchedulableClient(client, supportedJobNames);
    }

    public int count() {
        return clients.size();
    }

    private void addSchedulableClient(Client client, Set<String> supportedJobNames) {
        for (String supportedJobName : supportedJobNames) {
            addSchedulableClient(supportedJobName, client);
        }
        ServerContext.getInstance().getEventBroadcaster().publish(new ClientEvent(client, ClientEvent.Code.ADDED));
    }

    public void unregister(String ip, int port) {
        Assert.hasText(ip, "ip should not be empty.");

        Client client = findClient0(ip, port);
        if (client == null) {
            logger.error("Client {}:{} not found.", ip, port);
            return;
        }
        clients.remove(client);

        boolean isRemoved = doRemoveSchedulableClient(client);
        if (isRemoved) {
            ServerContext.getInstance().getEventBroadcaster().publish(new ClientEvent(client, ClientEvent.Code.REMOVED));
        }
    }

    public void addSchedulableClient(String jobName, String ip, int port) {
        Client client = findClient0(ip, port);
        if (client == null) {
            throw new IllegalArgumentException("Client [" + ip + ":" + port + "] has not been registered.");
        }

        addSchedulableClient(jobName, client);
    }

    private void addSchedulableClient(String jobName, Client client) {
        synchronized (schedulableClients) {
            Set<Client> clients = this.schedulableClients.get(jobName);
            if (clients == null) {
                clients = new HashSet<Client>();
                this.schedulableClients.put(jobName, clients);
            }
            clients.add(client);
        }
    }

    public Client findClient(String ip, int port) {
        for (Client client : clients) {
            if (client.getIp().equals(ip) && client.getPort() == port) {
                return (Client) client.clone();
            }
        }
        return null;
    }

    private Client findClient0(String ip, int port) {
        for (Client client : clients) {
            if (client.getIp().equals(ip) && client.getPort() == port) {
                return client;
            }
        }
        return null;
    }

    private Client findClient(Collection<Client> clients, String ip, int port) {
        for (Client client : clients) {
            if (client.getIp().equals(ip) && client.getPort() == port) {
                return client;
            }
        }
        return null;
    }

    public void removeSchedulableClient(String jobName, String ip, int port) {
        Set<Client> clients = schedulableClients.get(jobName);
        if (CollectionUtils.isEmpty(clients)) {
            return;
        }

        Client client = findClient(clients, ip, port);
        boolean isRemoved = clients.remove(client);
        if (isRemoved) {
            ServerContext.getInstance().getEventBroadcaster().publish(new ClientEvent(client, ClientEvent.Code.DISABLED));
        }
    }

    public void removeSchedulableClient(String ip, int port) {
        Client client = findClient0(ip, port);
        if (client == null) {
            throw new IllegalArgumentException("Client [" + ip + ":" + port + "] has not been registered.");
        }

        boolean isRemoved = doRemoveSchedulableClient(client);
        if (isRemoved) {
            ServerContext.getInstance().getEventBroadcaster().publish(new ClientEvent(client, ClientEvent.Code.DISABLED));
        }
    }

    private boolean doRemoveSchedulableClient(Client removedClient) {
        boolean isRemoved = false;
        Iterator<Map.Entry<String, Set<Client>>> mappingIterator = schedulableClients.entrySet().iterator();
        while (mappingIterator.hasNext()) {
            Map.Entry<String, Set<Client>> entry = mappingIterator.next();
            Set<Client> clients = entry.getValue();

            Iterator<Client> clientIterator = clients.iterator();
            while (clientIterator.hasNext()) {
                Client client = clientIterator.next();
                if (client.equals(removedClient)) {
                    clientIterator.remove();
                    isRemoved = true;
                }
            }

            if (CollectionUtils.isEmpty(clients)) {
                mappingIterator.remove();
            }
        }
        return isRemoved;
    }

    public List<Client> getAllClients() {
        return ImmutableList.copyOf(clients);
    }

    public List<Client> getSchedulableClients(String jobName) {
        Assert.hasText(jobName, "jobName should not be empty.");
        Set<Client> schedulables = schedulableClients.get(jobName);
        if (CollectionUtils.isEmpty(schedulables)) {
            return Collections.emptyList();
        } else {
            return ImmutableList.copyOf(schedulableClients.get(jobName));
        }
    }

    public Map<String, Set<Client>> getAllSchedulableClients() {
        return ImmutableMap.copyOf(schedulableClients);
    }

    public List<String> getSchedulableJobs(String ip, int port) {
        List<String> scheduableJobs = new ArrayList<String>();
        Client client = findClient0(ip, port);
        for (Map.Entry<String, Set<Client>> entry : schedulableClients.entrySet()) {
            Set<Client> clients = entry.getValue();
            if (CollectionUtils.isEmpty(clients)) {
                continue;
            }

            if (clients.contains(client)) {
                scheduableJobs.add(entry.getKey());
            }
        }
        return scheduableJobs;
    }

}
