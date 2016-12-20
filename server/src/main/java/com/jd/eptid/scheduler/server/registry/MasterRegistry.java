package com.jd.eptid.scheduler.server.registry;

import com.google.common.base.Throwables;
import com.jd.eptid.scheduler.core.domain.node.Node;
import com.jd.eptid.scheduler.core.exception.ZkException;
import com.jd.eptid.scheduler.core.master.MasterChooser;
import com.jd.eptid.scheduler.core.network.Registry;
import com.jd.eptid.scheduler.core.network.TransportReadyListener;
import com.jd.eptid.scheduler.core.zk.ZookeeperTransport;
import com.jd.eptid.scheduler.server.core.AppContext;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Created by classdan on 16-10-31.
 */
public abstract class MasterRegistry implements Registry, TransportReadyListener<ZooKeeper> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    protected static final String SERVER_NODES_PATH = "/scheduler/nodes/server";
    protected ZookeeperTransport zookeeperTransport = AppContext.getInstance().getZookeeperTransport();
    protected MasterChooser masterChooser;
    private CountDownLatch serverNodeExistsLatch = new CountDownLatch(1);

    public MasterRegistry() {
        AppContext.getInstance().getZookeeperTransport().addReadyListener(this);
    }

    protected void setMasterChooser(MasterChooser masterChooser) {
        this.masterChooser = masterChooser;
        AppContext.getInstance().setMasterChooser(this.masterChooser);
    }

    @Override
    public void register() {
        try {
            serverNodeExistsLatch.await();
        } catch (InterruptedException e) {
            // Ignore
        }

        logger.info("Register on zk server...");
        Node node = AppContext.getInstance().thisNode();
        doRegister(node);
    }

    protected abstract void doRegister(Node node);

    @Override
    public void onReady(ZooKeeper zooKeeper) {
        createServerNodeIfNecessary();
        addZkListener();
    }

    private void createServerNodeIfNecessary() {
        if (zookeeperTransport.exists(SERVER_NODES_PATH)) {
            serverNodeExistsLatch.countDown();
            return;
        }

        try {
            zookeeperTransport.createPersistentRecursively(SERVER_NODES_PATH);
            serverNodeExistsLatch.countDown();
        } catch (ZkException e) {
            logger.error("Failed to create server node.", e.getMessage());
            Throwables.propagate(e);
        }
    }

    protected abstract void addZkListener();

}
