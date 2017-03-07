package com.jd.eptid.scheduler.core.master;

import com.jd.eptid.scheduler.core.domain.node.Node;
import com.jd.eptid.scheduler.core.event.EventBroadcaster;
import com.jd.eptid.scheduler.core.event.MasterChangedEvent;
import com.jd.eptid.scheduler.core.zk.ZookeeperEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by classdan on 16-11-9.
 */
public abstract class ZkBasedMasterChooser implements MasterChooser {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    protected ZookeeperEndpoint zookeeperEndpoint;
    private Node master;
    private EventBroadcaster eventBroadcaster;
    private ReentrantLock lock = new ReentrantLock();

    public ZkBasedMasterChooser(ZookeeperEndpoint zookeeperEndpoint, EventBroadcaster eventBroadcaster) {
        this.zookeeperEndpoint = zookeeperEndpoint;
        this.eventBroadcaster = eventBroadcaster;
    }

    protected void setMaster(String nodePath) {
        if (nodePath == null) {
            master = null;
            notifyLost();
            return;
        }

        String nodeIdentity = zookeeperEndpoint.getData(nodePath);
        Node node = Node.parse(nodeIdentity);
        if (master == null || !master.equals(node)) {
            setMaster(node);
        }
    }

    private void setMaster(Node node) {
        logger.info("Master node changed: {}.", node);

        lock.lock();
        try {
            this.master = node;
            notifyNewMaster(master);
        } finally {
            lock.unlock();
        }
    }

    protected void notifyLost() {
        eventBroadcaster.publish(new MasterChangedEvent(null));
    }

    private void notifyNewMaster(Node masterNode) {
        eventBroadcaster.publish(new MasterChangedEvent(masterNode));
    }
}
