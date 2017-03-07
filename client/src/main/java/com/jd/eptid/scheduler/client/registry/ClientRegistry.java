package com.jd.eptid.scheduler.client.registry;

import com.google.common.base.Throwables;
import com.jd.eptid.scheduler.client.core.ClientContext;
import com.jd.eptid.scheduler.core.domain.node.Node;
import com.jd.eptid.scheduler.core.event.ZkEvent;
import com.jd.eptid.scheduler.core.event.ZkStateEvent;
import com.jd.eptid.scheduler.core.exception.ZkException;
import com.jd.eptid.scheduler.core.listener.ZkEventListener;
import com.jd.eptid.scheduler.core.network.Registry;
import com.jd.eptid.scheduler.core.zk.ZookeeperEndpoint;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by classdan on 16-11-11.
 */
public class ClientRegistry implements Registry, ZkEventListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String CLIENT_NODES_PATH = "/scheduler/nodes/clients";
    private ZookeeperEndpoint zookeeperEndpoint = ClientContext.getInstance().getZookeeperEndpoint();

    public ClientRegistry() {
        ClientContext.getInstance().getZookeeperEndpoint().addStateListener(this);
    }

    @Override
    public void register() {
        createClientNodeIfNecessary();

        Node thisNode = ClientContext.getInstance().thisNode();
        logger.info("Register on zk server: {}...", thisNode.getIdentity());
        zookeeperEndpoint.create(getClientNodePath(thisNode), null, CreateMode.EPHEMERAL, new AsyncCallback.StringCallback() {
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
        logger.info("Unregister this node on zk...");
        Node thisNode = ClientContext.getInstance().thisNode();
        String nodePath = getClientNodePath(thisNode);
        try {
            zookeeperEndpoint.delete(nodePath);
        } catch (Exception e) {
            logger.error("Failed to unregister this node.", e);
        }
    }

    private void createClientNodeIfNecessary() {
        if (zookeeperEndpoint.exists(CLIENT_NODES_PATH)) {
            return;
        }

        try {
            zookeeperEndpoint.createPersistentRecursively(CLIENT_NODES_PATH);
        } catch (ZkException e) {
            logger.error("Failed to create client node.", e.getMessage());
            Throwables.propagate(e);
        }
    }

    @Override
    public void onEvent(ZkEvent event) {
        if (event instanceof ZkStateEvent) {
            ZkStateEvent stateEvent = (ZkStateEvent) event;
            if (stateEvent.getCode() == ZkStateEvent.Code.CONNECTED) {
                register();
            }
        }
    }
}
