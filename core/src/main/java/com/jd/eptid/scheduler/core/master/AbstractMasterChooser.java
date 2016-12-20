package com.jd.eptid.scheduler.core.master;

import com.jd.eptid.scheduler.core.domain.node.Node;
import com.jd.eptid.scheduler.core.zk.ZookeeperTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by classdan on 16-11-9.
 */
public abstract class AbstractMasterChooser implements MasterChooser {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    protected ZookeeperTransport zookeeperTransport;
    private Node master;
    private Node thisNode;
    private List<MasterChangeListener> listeners = new LinkedList<MasterChangeListener>();
    private ReentrantLock lock = new ReentrantLock();

    public AbstractMasterChooser(ZookeeperTransport zookeeperTransport, Node thisNode) {
        this.zookeeperTransport = zookeeperTransport;
        this.thisNode = thisNode;
    }

    @Override
    public boolean isMaster() {
        return thisNode.equals(master);
    }

    @Override
    public void addListener(MasterChangeListener listener) {
        listeners.add(listener);
    }

    protected void setMaster(String nodePath) {
        String nodeIdentity = zookeeperTransport.getData(nodePath);
        Node node = Node.parse(nodeIdentity);

        if (master == null || !master.equals(node)) {
            setMaster(node);
        }
    }

    private void setMaster(Node node) {
        if (thisNode.equals(node)) {
            logger.info("This node has been elected as a master.");
        } else {
            logger.info("Master node changed: {}.", node);
        }

        lock.lock();
        try {
            this.master = node;
            notifyListeners(master);
        } finally {
            lock.unlock();
        }
    }

    private void notifyListeners(Node masterNode) {
        for (MasterChangeListener listener : listeners) {
            try {
                listener.onChange(masterNode);
            } catch (Exception e) {
                logger.error("Notify error.", e);
            }
        }
    }
}
