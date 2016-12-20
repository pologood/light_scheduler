package com.jd.eptid.scheduler.server.registry;

import com.jd.eptid.scheduler.core.domain.Event;
import com.jd.eptid.scheduler.core.domain.node.Node;
import com.jd.eptid.scheduler.core.master.MasterPreemptor;
import com.jd.eptid.scheduler.core.zk.NodeChangedListener;
import com.jd.eptid.scheduler.server.core.AppContext;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by classdan on 16-10-31.
 */
public class PreemptiveMasterRegistry extends MasterRegistry implements NodeChangedListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String MASTER_NODE_PATH = "/master";

    public PreemptiveMasterRegistry() {
        setMasterChooser(new MasterPreemptor(AppContext.getInstance().getZookeeperTransport(), AppContext.getInstance().thisNode()));
    }

    @Override
    protected void doRegister(final Node node) {
        zookeeperTransport.create(SERVER_NODES_PATH + MASTER_NODE_PATH, node.getIdentity(), CreateMode.EPHEMERAL, new AsyncCallback.StringCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, String name) {
                switch (KeeperException.Code.get(rc)) {
                    case OK:
                        masterChooser.choose(Arrays.asList(path));
                        break;
                    case NODEEXISTS:
                        logger.debug("Node {} exists.", path);
                        zookeeperTransport.getData(SERVER_NODES_PATH + MASTER_NODE_PATH);
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

    @Override
    protected void addZkListener() {
        zookeeperTransport.addNodeChangedListener(SERVER_NODES_PATH + MASTER_NODE_PATH, this);
    }

    @Override
    public void unregister() {
        // Need not to do unregister
    }

    @Override
    public void onChange(String path, Event event) {
        if (event == Event.REMOVED) {
            register();
        }
    }
}
