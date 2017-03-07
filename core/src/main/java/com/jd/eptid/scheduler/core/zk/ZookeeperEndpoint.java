package com.jd.eptid.scheduler.core.zk;

import com.google.common.base.Throwables;
import com.jd.eptid.scheduler.core.common.LifeCycle;
import com.jd.eptid.scheduler.core.common.ShutdownHook;
import com.jd.eptid.scheduler.core.config.Configuration;
import com.jd.eptid.scheduler.core.event.EventBroadcaster;
import com.jd.eptid.scheduler.core.event.ZChildrenEvent;
import com.jd.eptid.scheduler.core.event.ZNodeEvent;
import com.jd.eptid.scheduler.core.event.ZkStateEvent;
import com.jd.eptid.scheduler.core.exception.ZkException;
import com.jd.eptid.scheduler.core.exception.ZkException.ExistException;
import com.jd.eptid.scheduler.core.exception.ZkException.OtherException;
import com.jd.eptid.scheduler.core.exception.ZkException.ParentNotExistException;
import com.jd.eptid.scheduler.core.listener.ZkEventListener;
import com.jd.eptid.scheduler.core.utils.TimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by classdan on 16-10-31.
 */
public class ZookeeperEndpoint implements LifeCycle {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final int ORDER = 0;
    private static final String ZK_URL_KEY = "zk.connection.url";
    private static final String ZK_TIMEOUT_KEY = "zk.connection.timeout";
    private ZooKeeper zooKeeper;
    private volatile boolean isShutdown = false;
    private ReentrantLock lock = new ReentrantLock();
    private Condition connectedCondition = lock.newCondition();
    private Condition disconnectedCondition = lock.newCondition();
    private List<ZkEventListener> stateListeners = new CopyOnWriteArrayList<ZkEventListener>();
    private ConcurrentMap<String, List<ZkEventListener>> nodeChangedListeners = new ConcurrentHashMap<String, List<ZkEventListener>>();
    private ConcurrentMap<String, List<ZkEventListener>> childrenChangedListeners = new ConcurrentHashMap<String, List<ZkEventListener>>();
    private Thread zkGuardThread = null;
    private EventBroadcaster eventBroadcaster;

    public ZookeeperEndpoint(EventBroadcaster eventBroadcaster) {
        this.eventBroadcaster = eventBroadcaster;
        ShutdownHook.getInstance().addLifeCycleObject(this);
    }

    @Override
    public void start() {
        zkGuardThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isShutdown) {
                    lock.lock();
                    try {
                        connect();
                        disconnectedCondition.await();
                    } catch (IOException e) {
                        logger.error("Failed to connect zk server.");
                        TimeUtils.sleep(3, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        // Ignore
                    } finally {
                        lock.unlock();
                    }
                }
            }
        }, "zk-connection");
        zkGuardThread.setDaemon(true);
        zkGuardThread.start();
    }

    private void connect() throws IOException {
        if (zooKeeper != null && zooKeeper.getState().isAlive()) {
            logger.error("Zookeeper connection is alive.");
            return;
        }

        String zkConnectionUrl = Configuration.get(ZK_URL_KEY);
        int timeout = Configuration.getInteger(ZK_TIMEOUT_KEY, 3000);
        Assert.hasText(zkConnectionUrl);

        logger.info("Try to connect to zookeeper server...");
        zooKeeper = new ZooKeeper(zkConnectionUrl, timeout, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getType() == Event.EventType.None) {
                    switch (event.getState()) {
                        case SyncConnected:
                            logger.info("Connect to zk server successful.");
                            onConnected();
                            break;
                        case Disconnected:
                            logger.warn("Disconnected with zk server.");
                            onDisconnected();
                            break;
                        case Expired:
                            logger.warn("Zk session expired.");
                            onDisconnected();
                            break;
                        default:
                            break;
                    }
                }

                watchNodeChanged(event);
            }
        });
    }

    private void onConnected() {
        lock.lock();
        try {
            connectedCondition.signal();
            eventBroadcaster.broadcast(stateListeners, new ZkStateEvent(zooKeeper, ZkStateEvent.Code.CONNECTED));
        } finally {
            lock.unlock();
        }
    }

    private void onDisconnected() {
        lock.lock();
        try {
            disconnectedCondition.signal();
            eventBroadcaster.broadcast(stateListeners, new ZkStateEvent(zooKeeper, ZkStateEvent.Code.DISCONNECTED));
        } finally {
            lock.unlock();
        }
    }

    private void watchNodeChanged(WatchedEvent event) {
        String path = event.getPath();
        if (event.getType() == EventType.NodeCreated || event.getType() == EventType.NodeDeleted) {
            notifyNodeChanged(path, event.getType());
        } else if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
            notifyChildrenChanged(path);
        }
    }

    private void notifyNodeChanged(String path, EventType eventType) {
        exists(path);

        List<ZkEventListener> listeners = nodeChangedListeners.get(path);
        ZNodeEvent.Code code = recognizeEvent(eventType);
        eventBroadcaster.broadcast(listeners, new ZNodeEvent(path, code));
    }

    private ZNodeEvent.Code recognizeEvent(EventType eventType) {
        if (eventType == EventType.NodeCreated) {
            return ZNodeEvent.Code.ADD;
        } else if (eventType == EventType.NodeDeleted) {
            return ZNodeEvent.Code.REMOVED;
        } else if (eventType == EventType.NodeDataChanged) {
            return ZNodeEvent.Code.CHANGED;
        } else {
            throw new IllegalArgumentException("Unsupported event type: " + eventType);
        }
    }

    private void notifyChildrenChanged(String path) {
        List<String> children = getChildren(path);
        List<ZkEventListener> listeners = childrenChangedListeners.get(path);
        eventBroadcaster.broadcast(listeners, new ZChildrenEvent(path, children));
    }

    public void addStateListener(ZkEventListener listener) {
        stateListeners.add(listener);
    }

    public void addNodeChangedListener(String path, ZkEventListener listener) {
        synchronized (nodeChangedListeners) {
            List<ZkEventListener> listeners = this.nodeChangedListeners.get(path);
            if (listeners == null) {
                listeners = new LinkedList<ZkEventListener>();
                this.nodeChangedListeners.put(path, listeners);
            }
            listeners.add(listener);
        }

        exists(path);
    }

    public void addChildrenChangedListener(String path, ZkEventListener listener) {
        synchronized (childrenChangedListeners) {
            List<ZkEventListener> listeners = this.childrenChangedListeners.get(path);
            if (listeners == null) {
                listeners = new LinkedList<ZkEventListener>();
                this.childrenChangedListeners.put(path, listeners);
            }
            listeners.add(listener);
        }

        getChildren(path);
    }

    public void createPersistent(String path, String value, boolean sequential) throws ParentNotExistException, ExistException, OtherException {
        CreateMode createMode = sequential ? CreateMode.PERSISTENT_SEQUENTIAL : CreateMode.PERSISTENT;
        create(path, value, createMode);
    }

    public void createEphemeral(String path, String value, boolean sequential) throws OtherException, ParentNotExistException, ExistException {
        CreateMode createMode = sequential ? CreateMode.EPHEMERAL_SEQUENTIAL : CreateMode.EPHEMERAL;
        create(path, value, createMode);
    }

    private void create(String path, String value, CreateMode createMode) throws ParentNotExistException, ExistException, OtherException {
        checkConnection();

        try {
            zooKeeper.create(path, value == null ? null : value.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
        } catch (KeeperException e) {
            if (isNodeExistsException(e)) {
                throw new ExistException(path);
            }
            if (isNoNodeException(e)) {
                String parentPath = path.substring(0, path.lastIndexOf('/'));
                throw new ParentNotExistException(parentPath);
            }
            throw new OtherException(path, e.code());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void createPersistentRecursively(String path) throws ZkException {
        String[] hierarchicPaths = splitPath(path);
        for (String hierarchicPath : hierarchicPaths) {
            try {
                createPersistent(hierarchicPath, null, false);
            } catch (ExistException e) {
                //Ignore
            } catch (ZkException e) {
                throw e;
            }
        }
    }

    private String[] splitPath(String path) {
        Assert.isTrue(path.startsWith("/"), "Invalid zookeeper path.");

        String[] parts = path.split("/");
        String[] hierarchicPaths = new String[parts.length - 1];
        int i = 0;
        for (String part : parts) {
            if (StringUtils.isBlank(part)) {
                continue;
            }

            if (i == 0) {
                hierarchicPaths[i] = "/" + part;
            } else {
                hierarchicPaths[i] = hierarchicPaths[i - 1] + "/" + part;
            }
            ++i;
        }
        return hierarchicPaths;
    }

    public void create(String path, String value, CreateMode createMode, AsyncCallback.StringCallback stringCallback) {
        checkConnection();
        zooKeeper.create(path, value == null ? null : value.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode, stringCallback, null);
    }

    public boolean exists(String path) {
        checkConnection();

        try {
            return zooKeeper.exists(path, true) != null;
        } catch (KeeperException e) {
            if (isNoNodeException(e)) {
                return false;
            }
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void exists(String path, Watcher watcher, AsyncCallback.StatCallback statCallback) {
        checkConnection();
        zooKeeper.exists(path, watcher, statCallback, null);
    }

    public void delete(String path) throws KeeperException, InterruptedException {
        checkConnection();

        zooKeeper.delete(path, -1);
    }

    public List<String> getChildren(String path) {
        checkConnection();

        try {
            return zooKeeper.getChildren(path, true);
        } catch (KeeperException e) {
            if (isNoNodeException(e)) {
                return null;
            }
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void getChildren(String path, Watcher watcher, AsyncCallback.ChildrenCallback childrenCallback) {
        checkConnection();
        zooKeeper.getChildren(path, watcher, childrenCallback, null);
    }

    public String getData(String path) {
        checkConnection();

        try {
            byte[] data = zooKeeper.getData(path, true, null);
            if (data == null) {
                return null;
            } else {
                return new String(data);
            }
        } catch (KeeperException e) {
            throw new ZkException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void getData(String path, Watcher watcher, AsyncCallback.DataCallback dataCallback) {
        checkConnection();
        zooKeeper.getData(path, watcher, dataCallback, null);
    }

    private boolean isNoNodeException(KeeperException e) {
        return e.code() == KeeperException.Code.NONODE;
    }

    private boolean isNodeExistsException(KeeperException e) {
        return e.code() == KeeperException.Code.NODEEXISTS;
    }

    private void checkConnection() {
        if (zooKeeper == null || !zooKeeper.getState().isAlive()) {
            lock.lock();
            try {
                connectedCondition.await(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                //Ignore
            } finally {
                lock.unlock();
            }

            if (zooKeeper == null || !zooKeeper.getState().isAlive()) {
                throw new RuntimeException("Zk connection is not ready.");
            }
        }
    }

    @Override
    public void stop() {
        isShutdown = true;
        zkGuardThread.interrupt();

        close();
    }

    private void close() {
        if (zooKeeper != null) {
            try {
                zooKeeper.close();
            } catch (InterruptedException e) {
                Throwables.propagate(e);
            }
        }
    }

    @Override
    public int order() {
        return ORDER;
    }

}
