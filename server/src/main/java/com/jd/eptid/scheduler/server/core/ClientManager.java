package com.jd.eptid.scheduler.server.core;

import com.google.common.collect.ImmutableList;
import com.jd.eptid.scheduler.core.domain.node.Client;
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
    private ConcurrentMap<String, Set<Client>> workingClients = new ConcurrentHashMap<String, Set<Client>>();

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

    public void addSchedulableClient(Client client, Set<String> supportedJobNames) {
        for (String supportedJobName : supportedJobNames) {
            Set<Client> clients = this.schedulableClients.get(supportedJobName);
            if (clients == null) {
                clients = new HashSet<Client>();
                this.schedulableClients.put(supportedJobName, clients);
            }
            clients.add(client);
        }
    }

    public void unregister(String ip, int port) {
        Assert.hasText(ip, "ip should not be empty.");

        Client client = getClient(ip, port);
        if (client == null) {
            logger.error("Client {}:{} not found.", ip, port);
            return;
        }
        clients.remove(client);
        removeSchedulableClient(client);
    }

    public void removeSchedulableClient(Client removeClient) {
        Iterator<Map.Entry<String, Set<Client>>> iterator = schedulableClients.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Set<Client>> entry = iterator.next();
            Set<Client> clients = entry.getValue();
            for (Client client : clients) {
                if (client.equals(removeClient)) {
                    iterator.remove();
                }
            }
        }
    }

    public List<Client> getAllClients() {
        return ImmutableList.copyOf(clients);
    }

    public Client getClient(String ip, int port) {
        for (Client client : clients) {
            if (client.getIp().equals(ip) && client.getPort() == port) {
                return client;
            }
        }
        return null;
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

    public List<Client> getWorkingClients(String jobName) {
        Assert.hasText(jobName, "jobName should not be empty.");
        Set<Client> workings = workingClients.get(jobName);
        if (CollectionUtils.isEmpty(workings)) {
            return Collections.emptyList();
        } else {
            return ImmutableList.copyOf(workingClients.get(jobName));
        }
    }

    public boolean isWorkingClient(String jobName, Client client) {
        Set<Client> workingClients = this.workingClients.get(jobName);
        if (workingClients == null) {
            return false;
        }
        return workingClients.contains(client);
    }

    public void addWorkingClient(String jobName, Client client) {
        Set<Client> clients = workingClients.get(jobName);
        if (clients == null) {
            clients = new HashSet<Client>();
        }
        clients.add(client);
        workingClients.put(jobName, clients);
    }

    public void removeWorkingClient(String jobName, Client client) {
        this.workingClients.remove(jobName, client);
    }

    public int sizeOfSchedulableClients(String jobName) {
        Set<Client> clients = schedulableClients.get(jobName);
        if (clients == null) {
            return 0;
        }
        return clients.size();
    }

}
