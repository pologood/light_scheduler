package com.jd.eptid.scheduler.client.core;

import com.jd.eptid.scheduler.client.master.MasterWatcher;
import com.jd.eptid.scheduler.client.master.PreemptiveMasterWatcher;
import com.jd.eptid.scheduler.client.network.ClientTransport;
import com.jd.eptid.scheduler.client.network.WorkerClientTransport;
import com.jd.eptid.scheduler.client.processor.RunTaskMessageProcessor;
import com.jd.eptid.scheduler.client.processor.SplitJobMessageProcessor;
import com.jd.eptid.scheduler.client.registry.ClientRegistry;
import com.jd.eptid.scheduler.core.domain.message.MessageType;
import com.jd.eptid.scheduler.core.domain.node.Node;
import com.jd.eptid.scheduler.core.master.MasterChangeListener;
import com.jd.eptid.scheduler.core.network.TransportReadyListener;
import com.jd.eptid.scheduler.core.utils.NetworkUtils;
import com.jd.eptid.scheduler.core.zk.ZookeeperTransport;
import io.netty.channel.Channel;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Created by classdan on 16-9-12.
 */
public class ClientBootstrap implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware, TransportReadyListener<Channel>, MasterChangeListener {
    private static Logger logger = LoggerFactory.getLogger(ClientBootstrap.class);
    private ApplicationContext applicationContext;
    private ClientRegistry clientRegistry;

    public ClientBootstrap() {
        createTransport();
        registerMessageProcessors();
        clientRegistry = new ClientRegistry();
        startMasterWatcher();
    }

    private void startMasterWatcher() {
        MasterWatcher masterWatcher = new PreemptiveMasterWatcher();
        masterWatcher.addListener(this);
        AppContext.getInstance().setMasterWatcher(masterWatcher);
    }

    private void createTransport() {
        ClientTransport clientTransport = new WorkerClientTransport();
        clientTransport.addReadyListener(this);
        AppContext.getInstance().setClientTransport(clientTransport);

        ZookeeperTransport zookeeperTransport = new ZookeeperTransport();
        AppContext.getInstance().setZookeeperTransport(zookeeperTransport);
    }

    private void registerMessageProcessors() {
        AppContext.getInstance().addMessageProcessors(MessageType.Task_Split, new SplitJobMessageProcessor());
        AppContext.getInstance().addMessageProcessors(MessageType.Task_Run, new RunTaskMessageProcessor());
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            recognizeJobs();
            recognizeTaskRunners();
            start();
            registerShutdownHook();
        }
    }

    private void recognizeJobs() {
        Map<String, Job> jobSplitterBeans = applicationContext.getBeansOfType(Job.class);
        for (Job job : jobSplitterBeans.values()) {
            String supportJobName = job.name();
            Assert.hasText(supportJobName, "Support job name of job should not be null or empty.");
            AppContext.getInstance().addJob(job.name(), job);
        }
    }

    private void recognizeTaskRunners() {
        Map<String, Task> taskBeans = applicationContext.getBeansOfType(Task.class);
        for (Task task : taskBeans.values()) {
            String supportJobName = task.job();
            Assert.hasText(supportJobName, "Support job name of task should not be null or empty.");
            AppContext.getInstance().addTask(supportJobName, task);
        }
    }

    private void start() {
        logger.info("Initializing client...");
        AppContext.getInstance().getZookeeperTransport().start();
        AppContext.getInstance().getClientTransport().start();
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("Shutdown client...");
                AppContext.getInstance().getClientTransport().shutdown();
                AppContext.getInstance().getZookeeperTransport().shutdown();
            }
        }));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onReady(Channel channel) {
        Pair<String, Integer> ipAndPort = NetworkUtils.getIpAndPort(channel);
        AppContext.getInstance().changeNode(ipAndPort.getLeft(), ipAndPort.getRight());

        clientRegistry.register();
    }

    @Override
    public void onChange(Node masterNode) {
        Node oldMasterNode = AppContext.getInstance().getMasterNode();
        if (oldMasterNode == null || !oldMasterNode.equals(masterNode)) {
            AppContext.getInstance().setMasterNode(masterNode);

            ClientTransport clientTransport = AppContext.getInstance().getClientTransport();
            if (clientTransport.isAlive()) {
                clientTransport.disconnect();
            }
            clientTransport.connect();
        }
    }

}
