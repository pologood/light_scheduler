package com.jd.eptid.scheduler.core.test.domain;

import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.core.domain.node.Node;
import com.jd.eptid.scheduler.core.domain.node.Server;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by classdan on 16-11-9.
 */
public class NodeTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testGetIdentity() {
        Node server = new Server();
        server.setIp("127.0.0.1");
        server.setPort(6688);
        Assert.assertTrue(server.getIdentity() != null && server.getIdentity().length() > 0);
        logger.info(server.getIdentity());

        Client client = new Client();
        client.setIp("192.168.111.0");
        client.setPort(39020);
        client.setSupportJobs(new HashSet<String>(Arrays.asList("testJob")));
        Assert.assertTrue(client.getIdentity() != null && client.getIdentity().length() > 0);
        logger.info(client.getIdentity());
    }

    @Test
    public void testParse() {
        Node server = new Server();
        server.setIp("127.0.0.1");
        server.setPort(6688);
        String identity = server.getIdentity();
        logger.info(identity);

        Node parseNode = Node.parse(identity);
        Assert.assertNotNull(parseNode);
    }

    @Test
    public void testEquals() {
        Client client1 = new Client();
        client1.setIp("192.168.111.0");
        client1.setPort(39020);
        client1.setSupportJobs(new HashSet<String>(Arrays.asList("testJob")));

        Client client2 = new Client();
        client2.setIp("192.168.111.0");
        client2.setPort(39020);
        client2.setSupportJobs(new HashSet<String>(Arrays.asList("testJob11")));

        Client client3 = new Client();
        client3.setIp("192.168.111.90");
        client3.setPort(29033);
        client3.setSupportJobs(new HashSet<String>(Arrays.asList("Job222")));

        Assert.assertTrue(client1.equals(client2));
        Assert.assertTrue(!client1.equals(client3));
        Assert.assertTrue(!client2.equals(client3));
    }

}
