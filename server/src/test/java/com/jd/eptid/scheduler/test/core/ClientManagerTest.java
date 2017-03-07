package com.jd.eptid.scheduler.test.core;

import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.server.core.ClientManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by classdan on 16-9-26.
 */
public class ClientManagerTest {
    private ClientManager clientManager = new ClientManager();

    @Before
    public void prepareData() {
        Set<String> supportedJobNames = new HashSet<String>(Arrays.asList("Job1", "Job2"));
        clientManager.register("127.0.0.1", 8820, supportedJobNames);

        supportedJobNames = new HashSet<String>(Arrays.asList("Job1", "Job3"));
        clientManager.register("127.0.0.1", 8821, supportedJobNames);

        supportedJobNames = new HashSet<String>(Arrays.asList("Job2"));
        clientManager.register("127.0.0.1", 8822, supportedJobNames);

        Assert.assertTrue(clientManager.getAllClients().size() == 3);
    }

    @Test
    public void testRegister() {
        Set<String> supportedJobNames = new HashSet<String>(Arrays.asList("Job4", "Job5"));
        clientManager.register("127.0.0.1", 8829, supportedJobNames);
        Assert.assertTrue(clientManager.getAllClients().size() == 4);

        Client client = new Client();
        client.setIp("127.0.0.1");
        client.setPort(8829);
        Assert.assertTrue(clientManager.getAllClients().contains(client));
    }

    @Test
    public void testUnregister() {
        clientManager.unregister("127.0.0.1", 8820);
        Assert.assertTrue(clientManager.getAllClients().size() == 2);
        System.out.println(clientManager.getAllSchedulableClients());
        Assert.assertTrue(clientManager.getSchedulableClients("Job1").size() == 1);
        Assert.assertTrue(clientManager.getSchedulableClients("Job2").size() == 1);
        Assert.assertTrue(clientManager.getSchedulableClients("Job3").size() == 1);
    }

    @Test
    public void testUnregister2() {
        clientManager.unregister("127.0.0.1", 8820);
        clientManager.unregister("127.0.0.1", 8821);
        Assert.assertTrue(clientManager.getAllClients().size() == 1);
        System.out.println(clientManager.getAllSchedulableClients());
        Assert.assertTrue(clientManager.getSchedulableClients("Job1").size() == 0);
        Assert.assertTrue(clientManager.getSchedulableClients("Job2").size() == 1);
        Assert.assertTrue(clientManager.getSchedulableClients("Job3").size() == 0);
    }

    @Test
    public void testUnregister3() {
        clientManager.unregister("127.0.0.1", 8820);
        clientManager.unregister("127.0.0.1", 8821);
        clientManager.unregister("127.0.0.1", 8822);
        Assert.assertTrue(clientManager.getAllClients().size() == 0);
        System.out.println(clientManager.getAllSchedulableClients());
        Assert.assertTrue(clientManager.getSchedulableClients("Job1").size() == 0);
        Assert.assertTrue(clientManager.getSchedulableClients("Job2").size() == 0);
        Assert.assertTrue(clientManager.getSchedulableClients("Job3").size() == 0);
    }

}
