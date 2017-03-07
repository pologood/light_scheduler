package com.jd.eptid.scheduler.server.core;

import com.jd.eptid.scheduler.core.common.Context;
import com.jd.eptid.scheduler.core.config.Configuration;
import com.jd.eptid.scheduler.core.domain.node.Node;
import com.jd.eptid.scheduler.core.domain.node.Server;
import com.jd.eptid.scheduler.core.failover.FailoverStrategy;
import com.jd.eptid.scheduler.core.master.MasterChooser;
import com.jd.eptid.scheduler.core.utils.NetworkUtils;
import com.jd.eptid.scheduler.server.config.ConfigItem;
import com.jd.eptid.scheduler.server.dao.ScheduledJobDao;
import com.jd.eptid.scheduler.server.dao.ScheduledTaskDao;
import com.jd.eptid.scheduler.server.failover.JobFailover;
import com.jd.eptid.scheduler.server.job.JobScheduler;
import com.jd.eptid.scheduler.server.network.ServerTransport;
import org.springframework.util.Assert;

/**
 * Created by classdan on 16-9-27.
 */
public class ServerContext extends Context {
    private static ServerContext serverContext = new ServerContext();
    private Node thisNode;
    private ServerTransport serverTransport;
    private MasterChooser masterChooser;
    private ClientManager clientManager;
    private JobScheduler jobScheduler;
    private ScheduledJobDao scheduledJobDao;
    private ScheduledTaskDao scheduledTaskDao;
    private FailoverStrategy retryStrategy;
    private JobFailover jobFailover;

    private ServerContext() {
        Node node = new Server();
        String localIp = NetworkUtils.getLocalIpAddresses()[0];
        int port = Configuration.getInteger(ConfigItem.SERVICE_PORT, 9188);
        node.setIp(localIp);
        node.setPort(port);
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

    public static ServerContext getInstance() {
        return serverContext;
    }

    public ServerTransport getServerTransport() {
        return serverTransport;
    }

    public void setServerTransport(ServerTransport serverTransport) {
        this.serverTransport = serverTransport;
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

    public JobScheduler getJobScheduler() {
        return jobScheduler;
    }

    public void setJobScheduler(JobScheduler jobScheduler) {
        this.jobScheduler = jobScheduler;
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

    public JobFailover getJobFailover() {
        return jobFailover;
    }

    public void setJobFailover(JobFailover jobFailover) {
        this.jobFailover = jobFailover;
    }
}
