package com.jd.eptid.scheduler.server.registry;

import com.jd.eptid.scheduler.core.domain.node.Node;
import com.jd.eptid.scheduler.core.event.ZNodeEvent;
import com.jd.eptid.scheduler.core.event.ZkEvent;
import com.jd.eptid.scheduler.core.master.MasterPreemptor;
import com.jd.eptid.scheduler.server.core.ServerContext;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by classdan on 16-10-31.
 */
public class PreemptiveMasterRegistry extends MasterRegistry {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String MASTER_NODE_PATH = "/master";

    public PreemptiveMasterRegistry() {
        ServerContext serverContext = ServerContext.getInstance();
        setMasterChooser(new MasterPreemptor(serverContext.getZookeeperEndpoint(), serverContext.getEventBroadcaster()));
    }

    @Override
    protected void doRegister(final Node node) {
        zookeeperEndpoint.create(SERVER_NODES_PATH + MASTER_NODE_PATH, node.getIdentity(), CreateMode.EPHEMERAL, new AsyncCallback.StringCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, String name) {
                switch (KeeperException.Code.get(rc)) {
                    case OK:
                        masterChooser.choose(Arrays.asList(path));
                        break;
                    case NODEEXISTS:
                        logger.info("Node {} exists.", path);
                        zookeeperEndpoint.getData(SERVER_NODES_PATH + MASTER_NODE_PATH);
                        break;
                    case CONNECTIONLOSS:
                    case SESSIONEXPIRED:
                        logger.warn("Connection is lost or session expired.");
                        register();
                        break;
                    default:
                        logger.error("Create node error. {}, {}.", path, KeeperException.Code.get(rc));
                        break;
                }
            }
        });
    }

    @Override
    protected void addZkListener() {
        zookeeperEndpoint.addNodeChangedListener(SERVER_NODES_PATH + MASTER_NODE_PATH, this);
    }

    @Override
    public void unregister() {
        // Need not to do unregister
    }

    @Override
    protected void handleZkNodeEvent(ZkEvent event) {
        if (event instanceof ZNodeEvent) {
            ZNodeEvent znodeEvent = (ZNodeEvent) event;
            if (znodeEvent.getCode() == ZNodeEvent.Code.REMOVED) {
                register();
            }
        }
    }

}
