package com.jd.eptid.scheduler.server.registry;

import com.google.common.base.Throwables;
import com.jd.eptid.scheduler.core.domain.node.Node;
import com.jd.eptid.scheduler.core.event.ZkEvent;
import com.jd.eptid.scheduler.core.event.ZkStateEvent;
import com.jd.eptid.scheduler.core.exception.ZkException;
import com.jd.eptid.scheduler.core.listener.ZkEventListener;
import com.jd.eptid.scheduler.core.master.MasterChooser;
import com.jd.eptid.scheduler.core.network.Registry;
import com.jd.eptid.scheduler.core.zk.ZookeeperEndpoint;
import com.jd.eptid.scheduler.server.core.ServerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Created by classdan on 16-10-31.
 */
public abstract class MasterRegistry implements Registry, ZkEventListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    protected static final String SERVER_NODES_PATH = "/scheduler/nodes/server";
    protected ZookeeperEndpoint zookeeperEndpoint = ServerContext.getInstance().getZookeeperEndpoint();
    protected MasterChooser masterChooser;
    private CountDownLatch serverNodeExistsLatch = new CountDownLatch(1);

    public MasterRegistry() {
        ServerContext.getInstance().getZookeeperEndpoint().addStateListener(this);
    }

    protected void setMasterChooser(MasterChooser masterChooser) {
        this.masterChooser = masterChooser;
        ServerContext.getInstance().setMasterChooser(this.masterChooser);
    }

    @Override
    public void register() {
        try {
            serverNodeExistsLatch.await();
        } catch (InterruptedException e) {
            // Ignore
        }

        logger.info("Register on zk server...");
        Node node = ServerContext.getInstance().thisNode();
        doRegister(node);
    }

    protected abstract void doRegister(Node node);

    @Override
    public void onEvent(ZkEvent event) {
        if (event instanceof ZkStateEvent) {
            ZkStateEvent zkStateEvent = (ZkStateEvent) event;
            if (zkStateEvent.getCode() == ZkStateEvent.Code.CONNECTED) {
                serverNodeExistsLatch = new CountDownLatch(1);
                createServerNodeIfNecessary();
                addZkListener();
                register();
            }
        } else {
            handleZkNodeEvent(event);
        }
    }

    private void createServerNodeIfNecessary() {
        if (zookeeperEndpoint.exists(SERVER_NODES_PATH)) {
            serverNodeExistsLatch.countDown();
            return;
        }

        try {
            zookeeperEndpoint.createPersistentRecursively(SERVER_NODES_PATH);
            serverNodeExistsLatch.countDown();
        } catch (ZkException e) {
            logger.error("Failed to create server node.", e.getMessage());
            Throwables.propagate(e);
        }
    }

    protected abstract void handleZkNodeEvent(ZkEvent event);

    protected abstract void addZkListener();

}
