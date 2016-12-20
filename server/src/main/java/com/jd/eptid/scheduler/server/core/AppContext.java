package com.jd.eptid.scheduler.server.core;

import com.jd.eptid.scheduler.core.domain.node.Node;
import com.jd.eptid.scheduler.core.domain.node.Server;
import com.jd.eptid.scheduler.core.failover.FailoverStrategy;
import com.jd.eptid.scheduler.core.zk.ZookeeperTransport;
import com.jd.eptid.scheduler.server.chooser.ClientChooser;
import com.jd.eptid.scheduler.server.dao.ScheduledJobDao;
import com.jd.eptid.scheduler.server.dao.ScheduledTaskDao;
import com.jd.eptid.scheduler.core.master.MasterChooser;
import com.jd.eptid.scheduler.server.network.ServerTransport;
import org.springframework.util.Assert;

/**
 * Created by classdan on 16-9-27.
 */
public class AppContext {
    private static AppContext appContext = new AppContext();
    private Node thisNode;
    private ServerTransport serverTransport;
    private ZookeeperTransport zkTransport;
    private MasterChooser masterChooser;
    private ClientManager clientManager;
    private ClientChooser clientChooser;
    private ScheduledJobDao scheduledJobDao;
    private ScheduledTaskDao scheduledTaskDao;
    private FailoverStrategy retryStrategy;

    private AppContext() {
        Node node = new Server();
        node.setCreateTime(System.currentTimeMillis());
        thisNode = node;
    }

    public void changeNode(String ip, int port) {
        Assert.notNull(thisNode);
        thisNode.setIp(ip);
        thisNode.setPort(port);
    }

    public Node thisNode() {
        return thisNode;
    }

    public static AppContext getInstance() {
        return appContext;
    }

    public ServerTransport getServerTransport() {
        return serverTransport;
    }

    public void setServerTransport(ServerTransport serverTransport) {
        this.serverTransport = serverTransport;
    }

    public ZookeeperTransport getZookeeperTransport() {
        return zkTransport;
    }

    public void setZkTransport(ZookeeperTransport zkTransport) {
        this.zkTransport = zkTransport;
    }

    public MasterChooser getMasterChooser() {
        return masterChooser;
    }

    public void setMasterChooser(MasterChooser masterChooser) {
        this.masterChooser = masterChooser;
    }

    public ClientManager getClientManager() {
        return clientManager;
    }

    public void setClientManager(ClientManager clientManager) {
        this.clientManager = clientManager;
    }

    public ClientChooser getClientChooser() {
        return clientChooser;
    }

    public void setClientChooser(ClientChooser clientChooser) {
        this.clientChooser = clientChooser;
    }

    public ScheduledJobDao getScheduledJobDao() {
        return scheduledJobDao;
    }

    public void setScheduledJobDao(ScheduledJobDao scheduledJobDao) {
        this.scheduledJobDao = scheduledJobDao;
    }

    public ScheduledTaskDao getScheduledTaskDao() {
        return scheduledTaskDao;
    }

    public void setScheduledTaskDao(ScheduledTaskDao scheduledTaskDao) {
        this.scheduledTaskDao = scheduledTaskDao;
    }

    public FailoverStrategy getRetryStrategy() {
        return retryStrategy;
    }

    public void setRetryStrategy(FailoverStrategy retryStrategy) {
        this.retryStrategy = retryStrategy;
    }
}
