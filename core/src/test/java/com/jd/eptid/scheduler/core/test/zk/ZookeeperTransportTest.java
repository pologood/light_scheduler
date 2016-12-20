package com.jd.eptid.scheduler.core.test.zk;

import com.jd.eptid.scheduler.core.zk.ChildrenChangedListener;
import com.jd.eptid.scheduler.core.zk.ZookeeperTransport;
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
public class ZookeeperTransportTest implements ChildrenChangedListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ZookeeperTransport zookeeperTransport = new ZookeeperTransport();

    @Before
    public void init() throws Exception {
        zookeeperTransport.start();
        zookeeperTransport.addChildrenChangedListener("/test", this);
    }

    @Test
    public void testListen() throws InterruptedException {
        TimeUnit.HOURS.sleep(1);
    }

    @Test
    public void testCreatePersistentRecursively() {
        String path = "/newTest/good/one";
        zookeeperTransport.createPersistentRecursively("/newTest/good/one");
        logger.info("Create [{}] successful.", path);
    }

    @After
    public void destroy() throws Exception {
        zookeeperTransport.shutdown();
    }

    @Override
    public void onChange(String path, List<String> children) {
        logger.info("Children changed: {}.", path);
        logger.info("Current children: {}.", children);
    }
}
