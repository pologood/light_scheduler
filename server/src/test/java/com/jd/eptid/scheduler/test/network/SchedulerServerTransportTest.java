package com.jd.eptid.scheduler.test.network;

import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.core.domain.message.Message;
import com.jd.eptid.scheduler.core.domain.message.MessageType;
import com.jd.eptid.scheduler.core.exception.CommunicationException;
import com.jd.eptid.scheduler.server.core.AppContext;
import com.jd.eptid.scheduler.server.core.ClientManager;
import com.jd.eptid.scheduler.core.failover.FailoverStrategy;
import com.jd.eptid.scheduler.core.failover.RetryStrategy;
import com.jd.eptid.scheduler.server.network.ChannelHolder;
import com.jd.eptid.scheduler.server.network.SchedulerServerTransport;
import com.jd.eptid.scheduler.server.network.ServerTransport;
import io.netty.channel.Channel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by classdan on 16-9-26.
 */
public class SchedulerServerTransportTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ServerTransport serverTransport;
    private ClientManager clientManager;
    private FailoverStrategy failoverStrategy;

    @Before
    public void init() throws Exception {
        AppContext appContext = AppContext.getInstance();
        appContext.setClientManager(new ClientManager());
        appContext.setServerTransport(new SchedulerServerTransport());

        serverTransport = appContext.getServerTransport();
        clientManager = appContext.getClientManager();
        failoverStrategy = new RetryStrategy();

        serverTransport.start();
    }

    @Test
    public void testStandby() throws InterruptedException {
        TimeUnit.MINUTES.sleep(5);
    }

    @Test
    public void testSend() throws InterruptedException, ExecutionException, TimeoutException {
        TimeUnit.SECONDS.sleep(100);

        Message message = new Message();
        message.setType(MessageType.Task_Split.getCode());
        message.setContent("hello");

        Client client = clientManager.getAllClients().get(0);
        Channel channel = ChannelHolder.findChannel(client);
        Message response = serverTransport.send(channel, message, 3, TimeUnit.SECONDS);
        Assert.assertNotNull(response);
        logger.info(response.toString());
    }

    @Test(expected = CommunicationException.class)
    public void testRetrySend() throws Exception {
        Message message = new Message();
        message.setType(MessageType.Task_Split.getCode());
        message.setContent("hello");

        TimeUnit.SECONDS.sleep(100);

        Client client = clientManager.getAllClients().get(0);
        Channel channel = ChannelHolder.findChannel(client);
        Message response = serverTransport.send(channel, message, 3, TimeUnit.SECONDS);
    }

    @After
    public void destroy() throws Exception {
        serverTransport.shutdown();
    }

}
