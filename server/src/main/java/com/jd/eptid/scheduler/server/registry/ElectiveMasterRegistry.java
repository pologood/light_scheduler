package com.jd.eptid.scheduler.server.registry;

import com.jd.eptid.scheduler.core.domain.node.Node;
import com.jd.eptid.scheduler.core.master.MasterElector;
import com.jd.eptid.scheduler.core.utils.MiscUtils;
import com.jd.eptid.scheduler.core.zk.ChildrenChangedListener;
import com.jd.eptid.scheduler.server.core.AppContext;
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
public class ElectiveMasterRegistry extends MasterRegistry implements ChildrenChangedListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String MASTER_NODE_PREFIX = "/server-";

    public ElectiveMasterRegistry() {
        setMasterChooser(new MasterElector(AppContext.getInstance().getZookeeperTransport(), AppContext.getInstance().thisNode()));
    }

    @Override
    protected void doRegister(final Node node) {
        zookeeperTransport.create(SERVER_NODES_PATH + MASTER_NODE_PREFIX, node.getIdentity(), CreateMode.EPHEMERAL_SEQUENTIAL, new AsyncCallback.StringCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, String name) {
                switch (KeeperException.Code.get(rc)) {
                    case OK:
                        electMaster();
                        break;
                    default:
                        MiscUtils.sleep(3, TimeUnit.SECONDS);
                        register();
                        break;
                }
            }
        });
    }

    @Override
    protected void addZkListener() {
        zookeeperTransport.addChildrenChangedListener(SERVER_NODES_PATH, this);
    }

    private void electMaster() {
        List<String> children = zookeeperTransport.getChildren(SERVER_NODES_PATH);
        if (children.isEmpty()) {
            return;
        }
        masterChooser.choose(children);
    }

    @Override
    public void unregister() {
        // Need not to do unregister
    }

    @Override
    public void onChange(String path, List<String> children) {
        logger.info("Server nodes changed, reelect master...");
        electMaster();
    }
}
