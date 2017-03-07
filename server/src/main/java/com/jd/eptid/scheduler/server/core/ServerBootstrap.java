package com.jd.eptid.scheduler.server.core;

import com.jd.eptid.scheduler.core.event.NetworkStateEvent;
import com.jd.eptid.scheduler.core.failover.RetryStrategy;
import com.jd.eptid.scheduler.core.listener.NetworkEventListener;
import com.jd.eptid.scheduler.server.dao.ScheduledJobDao;
import com.jd.eptid.scheduler.server.dao.ScheduledTaskDao;
import com.jd.eptid.scheduler.server.failover.JobFailover;
import com.jd.eptid.scheduler.server.handler.ServerChannelHandler;
import com.jd.eptid.scheduler.server.job.JobScheduler;
import com.jd.eptid.scheduler.server.network.SchedulerServerTransport;
import com.jd.eptid.scheduler.server.registry.MasterRegistry;
import com.jd.eptid.scheduler.server.registry.PreemptiveMasterRegistry;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Created by classdan on 16-9-7.
 */
@Component
public class ServerBootstrap implements ApplicationListener, ApplicationContextAware, NetworkEventListener {
    private MasterRegistry masterRegistry;
    private ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent && ((ContextRefreshedEvent) event).getApplicationContext().getParent() == null) {
            initAppContext();
            createRegistry();
            registerListeners();
            startTransport();
        }
    }

    private void initAppContext() {
        ServerContext serverContext = ServerContext.getInstance();
        serverContext.setClientManager(applicationContext.getBean(ClientManager.class));
        serverContext.setJobScheduler(applicationContext.getBean(JobScheduler.class));
        ServerChannelHandler serverChannelHandler = applicationContext.getBean(ServerChannelHandler.class);
        serverContext.setServerTransport(new SchedulerServerTransport(serverContext.getEventBroadcaster(), serverChannelHandler));
        serverContext.setScheduledJobDao(applicationContext.getBean(ScheduledJobDao.class));
        serverContext.setScheduledTaskDao(applicationContext.getBean(ScheduledTaskDao.class));
        serverContext.setRetryStrategy(applicationContext.getBean(RetryStrategy.class));
        serverContext.setJobFailover(new JobFailover());
    }

    private void createRegistry() {
        masterRegistry = new PreemptiveMasterRegistry();
    }

    private void registerListeners() {
        ServerContext.getInstance().getEventBroadcaster().register(NetworkStateEvent.class, this);
    }

    private void startTransport() {
        ServerContext.getInstance().getZookeeperEndpoint().start();
        ServerContext.getInstance().getServerTransport().start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onEvent(NetworkStateEvent event) {
        switch (event.getCode()) {
            case READY:
                break;
            case UNAVAILABLE:
                break;
            default:
                break;
        }
    }
}
