package com.jd.eptid.scheduler.core.test.zk;

import com.jd.eptid.scheduler.core.event.*;
import com.jd.eptid.scheduler.core.listener.ZkEventListener;
import com.jd.eptid.scheduler.core.zk.ZookeeperEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by XiaoDan on 2016/3/10.
 */
public class ZookeeperEndpointTest implements ZkEventListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ZookeeperEndpoint zookeeperEndpoint = new ZookeeperEndpoint(new AsyncEventBroadcaster());

    @Before
    public void init() throws Exception {
        zookeeperEndpoint.start();
        zookeeperEndpoint.addStateListener(this);
        zookeeperEndpoint.addNodeChangedListener("/test", this);
        zookeeperEndpoint.addChildrenChangedListener("/test", this);
    }

    @Test
    public void testListen() throws InterruptedException {
        TimeUnit.HOURS.sleep(1);
    }

    @Test
    public void testCreatePersistentRecursively() {
        String path = "/newTest/good/one";
        zookeeperEndpoint.createPersistentRecursively("/newTest/good/one");
        logger.info("Create [{}] successful.", path);
    }

    @After
    public void destroy() throws Exception {
        zookeeperEndpoint.stop();
    }

    @Override
    public void onEvent(ZkEvent event) {
        if (event instanceof ZChildrenEvent) {
            ZChildrenEvent zChildrenEvent = (ZChildrenEvent) event;
            logger.info("Children changed: {}.", zChildrenEvent.getPath());
            logger.info("Current children: {}.", zChildrenEvent.getChildren());
        } else if (event instanceof ZNodeEvent) {
            ZNodeEvent zNodeEvent = (ZNodeEvent) event;
            logger.info("Node changed: {}.", zNodeEvent.getPath());
            logger.info("Code: {}.", zNodeEvent.getCode());
        } else if (event instanceof ZkStateEvent) {
            ZkStateEvent zkStateEvent = (ZkStateEvent) event;
            logger.info("State changed: {}.", zkStateEvent.getCode());
        }
    }
}
