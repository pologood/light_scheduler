package com.jd.eptid.scheduler.server.chooser;

import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.server.core.ClientManager;
import com.jd.eptid.scheduler.server.loadbalance.LoadBalance;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by classdan on 16-9-22.
 */
@Component
public class SimpleClientChooser implements ClientChooser {
    @Resource
    private ClientManager clientManager;
    @Resource(name = "randomLoadBalance")
    private LoadBalance<Client> loadBalance;
    private Lock lock = new ReentrantLock();
    private int maxRetryTimes = 3;

    @Override
    public Client chooseAndOccupy(String jobName) {
        int retries = 0;
        while (true) {
            if (retries > maxRetryTimes) {
                return null;
            }

            List<Client> schedulableClients = clientManager.getSchedulableClients(jobName);
            List<Client> workingClients = clientManager.getWorkingClients(jobName);

            List<Client> availableClients = new LinkedList(schedulableClients);
            availableClients.removeAll(workingClients);
            if (availableClients.isEmpty()) {
                ++retries;
                continue;
            }

            Client client = loadBalance.select(availableClients, null);
            allocate(jobName, client);
            return client;
        }
    }

    private void allocate(String jobName, Client client) {
        lock.lock();
        try {
            if (clientManager.isWorkingClient(jobName, client)) {
                clientManager.addWorkingClient(jobName, client);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void release(String jobName, Client client) {
        lock.lock();
        try {
            clientManager.removeWorkingClient(jobName, client);
        } finally {
            lock.unlock();
        }
    }

}
