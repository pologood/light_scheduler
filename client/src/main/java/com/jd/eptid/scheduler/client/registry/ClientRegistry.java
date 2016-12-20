package com.jd.eptid.scheduler.client.registry;

import com.google.common.base.Throwables;
import com.jd.eptid.scheduler.client.core.AppContext;
import com.jd.eptid.scheduler.core.domain.node.Node;
import com.jd.eptid.scheduler.core.exception.ZkException;
import com.jd.eptid.scheduler.core.network.Registry;
import com.jd.eptid.scheduler.core.network.TransportReadyListener;
import com.jd.eptid.scheduler.core.zk.ZookeeperTransport;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Created by classdan on 16-11-11.
 */
public class ClientRegistry implements Registry, TransportReadyListener<ZooKeeper> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String CLIENT_NODES_PATH = "/scheduler/nodes/clients";
    private ZookeeperTransport zookeeperTransport = AppContext.getInstance().getZookeeperTransport();
    private CountDownLatch clientsNodeExistsLatch = new CountDownLatch(1);

    public ClientRegistry() {
        AppContext.getInstance().getZookeeperTransport().addReadyListener(this);
    }

    @Override
    public void register() {
        try {
            clientsNodeExistsLatch.await();
        } catch (InterruptedException e) {
            // Ignore
        }

        logger.info("Register on zk server...");
        Node thisNode = AppContext.getInstance().thisNode();
        zookeeperTransport.create(getClientNodePath(thisNode), null, CreateMode.EPHEMERAL, new AsyncCallback.StringCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, String name) {
                switch (KeeperException.Code.get(rc)) {
                    case OK:
                        logger.info("Register on zk successful.");
                        break;
                    case NODEEXISTS:
                        logger.info("Node already exists.");
                        break;
                    case CONNECTIONLOSS:
                    case SESSIONEXPIRED:
                        logger.debug("Connection is lost or session expired.");
                        register();
                        break;
                    default:
                        logger.error("Create node error. {}, {}.", path, KeeperException.Code.get(rc));
                        break;
                }
            }
        });
    }

    private String getClientNodePath(Node node) {
        return CLIENT_NODES_PATH + "/" + node.getIdentity();
    }

    @Override
    public void unregister() {
        // Nothing to do.
    }

    @Override
    public void onReady(ZooKeeper zooKeeper) {
        createClientNodeIfNecessary();
    }

    private void createClientNodeIfNecessary() {
        if (zookeeperTransport.exists(CLIENT_NODES_PATH)) {
            clientsNodeExistsLatch.countDown();
            return;
        }

        try {
            zookeeperTransport.createPersistentRecursively(CLIENT_NODES_PATH);
            clientsNodeExistsLatch.countDown();
        } catch (ZkException e) {
            logger.error("Failed to create client node.", e.getMessage());
            Throwables.propagate(e);
        }
    }
}
