package com.jd.eptid.scheduler.server.registry;

import com.jd.eptid.scheduler.core.domain.node.Node;
import com.jd.eptid.scheduler.core.event.ZChildrenEvent;
import com.jd.eptid.scheduler.core.event.ZkEvent;
import com.jd.eptid.scheduler.core.master.MasterElector;
import com.jd.eptid.scheduler.core.utils.TimeUtils;
import com.jd.eptid.scheduler.server.core.ServerContext;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by classdan on 16-10-31.
 */
public class ElectiveMasterRegistry extends MasterRegistry {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String MASTER_NODE_PREFIX = "/server-";

    public ElectiveMasterRegistry() {
        ServerContext serverContext = ServerContext.getInstance();
        setMasterChooser(new MasterElector(serverContext.getZookeeperEndpoint(), serverContext.getEventBroadcaster()));
    }

    @Override
    protected void doRegister(final Node node) {
        zookeeperEndpoint.create(SERVER_NODES_PATH + MASTER_NODE_PREFIX, node.getIdentity(), CreateMode.EPHEMERAL_SEQUENTIAL, new AsyncCallback.StringCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, String name) {
                switch (KeeperException.Code.get(rc)) {
                    case OK:
                        electMaster();
                        break;
                    default:
                        TimeUtils.sleep(3, TimeUnit.SECONDS);
                        register();
                        break;
                }
            }
        });
    }

    @Override
    protected void handleZkNodeEvent(ZkEvent event) {
        if (event instanceof ZChildrenEvent) {
            logger.info("Server nodes changed, reelect master...");
            electMaster();
        }
    }

    @Override
    protected void addZkListener() {
        zookeeperEndpoint.addChildrenChangedListener(SERVER_NODES_PATH, this);
    }

    private void electMaster() {
        List<String> children = zookeeperEndpoint.getChildren(SERVER_NODES_PATH);
        if (children.isEmpty()) {
            return;
        }
        masterChooser.choose(children);
    }

    @Override
    public void unregister() {
        // Need not to do unregister
    }
}
