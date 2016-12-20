package com.jd.eptid.scheduler.server.core;

import com.google.common.base.Throwables;
import com.jd.eptid.scheduler.core.config.Configuration;
import com.jd.eptid.scheduler.core.domain.job.Job;
import com.jd.eptid.scheduler.core.domain.node.Node;
import com.jd.eptid.scheduler.core.exception.ScheduleException;
import com.jd.eptid.scheduler.core.failover.RetryStrategy;
import com.jd.eptid.scheduler.core.master.MasterChangeListener;
import com.jd.eptid.scheduler.core.network.TransportReadyListener;
import com.jd.eptid.scheduler.core.utils.NetworkUtils;
import com.jd.eptid.scheduler.core.zk.ZookeeperTransport;
import com.jd.eptid.scheduler.server.chooser.ClientChooser;
import com.jd.eptid.scheduler.server.config.ConfigItem;
import com.jd.eptid.scheduler.server.dao.ScheduledJobDao;
import com.jd.eptid.scheduler.server.dao.ScheduledTaskDao;
import com.jd.eptid.scheduler.server.job.JobManager;
import com.jd.eptid.scheduler.server.job.JobScheduler;
import com.jd.eptid.scheduler.server.network.ServerTransport;
import com.jd.eptid.scheduler.server.registry.MasterRegistry;
import com.jd.eptid.scheduler.server.registry.PreemptiveMasterRegistry;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by classdan on 16-9-7.
 */
@Component
public class ServerBootstrap implements ApplicationListener, ApplicationContextAware, TransportReadyListener<Channel>, MasterChangeListener {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource
    private JobManager jobManager;
    @Resource
    private JobScheduler jobScheduler;
    private MasterRegistry masterRegistry;
    private ApplicationContext applicationContext;
    private boolean isStarted = false;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent && ((ContextRefreshedEvent) event).getApplicationContext().getParent() == null) {
            initAppContext();
            createRegistry();
            startTransport();
            registerShutdownHook();
        }
    }

    private void initAppContext() {
        AppContext appContext = AppContext.getInstance();
        appContext.setZkTransport(new ZookeeperTransport());
        appContext.setClientManager(applicationContext.getBean(ClientManager.class));
        appContext.setClientChooser(applicationContext.getBean(ClientChooser.class));
        appContext.setServerTransport(applicationContext.getBean(ServerTransport.class));
        appContext.setScheduledJobDao(applicationContext.getBean(ScheduledJobDao.class));
        appContext.setScheduledTaskDao(applicationContext.getBean(ScheduledTaskDao.class));
        appContext.setRetryStrategy(applicationContext.getBean(RetryStrategy.class));
    }

    private void createRegistry() {
        masterRegistry = new PreemptiveMasterRegistry();
        AppContext.getInstance().getMasterChooser().addListener(this);
    }

    private void startTransport() {
        AppContext.getInstance().getServerTransport().addReadyListener(this);

        AppContext.getInstance().getZookeeperTransport().start();
        AppContext.getInstance().getServerTransport().start();
    }

    private void startScheduler() {
        logger.info("Start scheduler...");
        try {
            jobScheduler.start();
            schedule();
            isStarted = true;
            logger.info("Start scheduler successful.");
        } catch (Exception e) {
            logger.error("Failed to start scheduler. " + Configuration.listAll(), e);
            Throwables.propagate(e);
        }
    }

    private void schedule() {
        try {
            List<Job> jobs = jobManager.getAvailableJobs();
            scheduleJobs(jobs);
        } catch (Exception e) {
            logger.error("Failed to start job schedulers.", e);
            Throwables.propagate(e);
        }
    }

    private void scheduleJobs(List<Job> jobs) {
        for (final Job job : jobs) {
            logger.info("Starting job scheduler for job: {}...", job.getName());

            try {
                jobScheduler.submit(job);
            } catch (ScheduleException e) {
                logger.error("No jobExecutor found for job: {}.", job);
                continue;
            }

            logger.info("Start job scheduler for job: {} successful.", job.getName());
        }
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("Stop server...");
                try {
                    stop();
                } catch (Exception e) {
                    logger.error("Failed to stop the server.", e);
                }
            }
        }));
    }

    private void stop() {
        stopScheduler();

        ServerTransport serverTransport = AppContext.getInstance().getServerTransport();
        ZookeeperTransport zookeeperTransport = AppContext.getInstance().getZookeeperTransport();
        if (zookeeperTransport != null) {
            zookeeperTransport.shutdown();
        }
        if (serverTransport != null) {
            serverTransport.shutdown();
        }
    }

    private void stopScheduler() {
        logger.info("Stop scheduler...");
        if (jobScheduler != null) {
            jobScheduler.stop();
        }
        isStarted = false;
        logger.info("Stop scheduler successful.");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onChange(Node masterNode) {
        if (AppContext.getInstance().thisNode().equals(masterNode) && !isStarted) { //Only master should start scheduler when system bootstrapped
            startScheduler();
        } else {
            stopScheduler();
        }
    }

    @Override
    public void onReady(Channel channel) {
        /*Pair<String, Integer> ipAndPort = NetworkUtils.getIpAndPort(channel);
        AppContext.getInstance().changeNode(ipAndPort.getLeft(), ipAndPort.getRight());*/
        String localIp = NetworkUtils.getLocalIpAddresses()[0];
        int port = Configuration.getInteger(ConfigItem.SERVICE_PORT, 9188);
        AppContext.getInstance().changeNode(localIp, port);

        masterRegistry.register();
    }
}
