package com.jd.eptid.scheduler.client.core;

import com.jd.eptid.scheduler.client.master.MasterWatcher;
import com.jd.eptid.scheduler.client.master.PreemptiveMasterWatcher;
import com.jd.eptid.scheduler.client.network.ClientTransport;
import com.jd.eptid.scheduler.client.network.WorkerClientTransport;
import com.jd.eptid.scheduler.client.processor.RunTaskMessageProcessor;
import com.jd.eptid.scheduler.client.processor.SplitJobMessageProcessor;
import com.jd.eptid.scheduler.client.registry.ClientRegistry;
import com.jd.eptid.scheduler.core.domain.message.MessageType;
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
public class ClientBootstrap implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {
    private static Logger logger = LoggerFactory.getLogger(ClientBootstrap.class);
    private ApplicationContext applicationContext;

    public ClientBootstrap() {
        createTransport();
        registerMessageProcessors();
        createRegistry();
        createMasterWatcher();
    }

    private void createTransport() {
        ClientTransport clientTransport = new WorkerClientTransport();
        ClientContext.getInstance().setClientTransport(clientTransport);
    }

    private void registerMessageProcessors() {
        ClientContext.getInstance().addMessageProcessors(MessageType.Task_Split, new SplitJobMessageProcessor());
        ClientContext.getInstance().addMessageProcessors(MessageType.Task_Run, new RunTaskMessageProcessor());
    }

    private void createRegistry() {
        ClientRegistry clientRegistry = new ClientRegistry();
        ClientContext.getInstance().setClientRegistry(clientRegistry);
    }

    private void createMasterWatcher() {
        MasterWatcher masterWatcher = new PreemptiveMasterWatcher();
        ClientContext.getInstance().setMasterWatcher(masterWatcher);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            recognizeJobs();
            recognizeTaskRunners();
            start();
        }
    }

    private void recognizeJobs() {
        Map<String, Job> jobSplitterBeans = applicationContext.getBeansOfType(Job.class);
        for (Job job : jobSplitterBeans.values()) {
            String supportJobName = job.name();
            Assert.hasText(supportJobName, "Support job name of job should not be null or empty.");
            ClientContext.getInstance().addJob(job.name(), job);
        }
    }

    private void recognizeTaskRunners() {
        Map<String, Task> taskBeans = applicationContext.getBeansOfType(Task.class);
        for (Task task : taskBeans.values()) {
            String supportJobName = task.job();
            Assert.hasText(supportJobName, "Support job name of task should not be null or empty.");
            ClientContext.getInstance().addTask(supportJobName, task);
        }
    }

    private void start() {
        logger.info("Initializing client...");
        ClientContext.getInstance().getZookeeperEndpoint().start();
        ClientContext.getInstance().getClientTransport().start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
