package com.jd.eptid.scheduler.client.core;

import com.jd.eptid.scheduler.client.master.MasterWatcher;
import com.jd.eptid.scheduler.client.network.ClientTransport;
import com.jd.eptid.scheduler.core.domain.message.MessageType;
import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.core.domain.node.Node;
import com.jd.eptid.scheduler.core.processor.MessageProcessor;
import com.jd.eptid.scheduler.core.zk.ZookeeperTransport;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by classdan on 16-9-30.
 */
public class AppContext {
    private static AppContext appContext = new AppContext();
    private ClientTransport clientTransport;
    private ZookeeperTransport zookeeperTransport;
    private Node thisNode;
    private Node masterNode;
    private MasterWatcher masterWatcher;
    private final Map<MessageType, MessageProcessor> messageProcessors = new HashMap<MessageType, MessageProcessor>();
    private final Map<String, Job> jobs = new HashMap<String, Job>();
    private final Map<String, Task> tasks = new HashMap<String, Task>();

    private AppContext() {
        Client node = new Client();
        node.setSupportJobs(new HashSet<String>());
        node.setCreateTime(System.currentTimeMillis());
        thisNode = node;
    }

    public static AppContext getInstance() {
        return appContext;
    }

    public ClientTransport getClientTransport() {
        return clientTransport;
    }

    public void setClientTransport(ClientTransport clientTransport) {
        this.clientTransport = clientTransport;
    }

    public ZookeeperTransport getZookeeperTransport() {
        return zookeeperTransport;
    }

    public void setZookeeperTransport(ZookeeperTransport zookeeperTransport) {
        this.zookeeperTransport = zookeeperTransport;
    }

    public void changeNode(String ip, int port) {
        Assert.notNull(thisNode);
        thisNode.setIp(ip);
        thisNode.setPort(port);
    }

    public Node thisNode() {
        return thisNode;
    }

    public Node getMasterNode() {
        return masterNode;
    }

    public void setMasterNode(Node masterNode) {
        this.masterNode = masterNode;
    }

    public MasterWatcher getMasterWatcher() {
        return masterWatcher;
    }

    public void setMasterWatcher(MasterWatcher masterWatcher) {
        this.masterWatcher = masterWatcher;
    }

    public Map<MessageType, MessageProcessor> getMessageProcessors() {
        return messageProcessors;
    }

    public void addMessageProcessors(MessageType type, MessageProcessor messageProcessor) {
        messageProcessors.put(type, messageProcessor);
    }

    public MessageProcessor getMessageProcessor(MessageType type) {
        return messageProcessors.get(type);
    }

    public Map<String, Job> getJobs() {
        return jobs;
    }

    public void addJob(String jobName, Job job) {
        jobs.put(jobName, job);
        ((Client) thisNode).getSupportJobs().add(jobName);
    }

    public Job getJob(String jobName) {
        return jobs.get(jobName);
    }

    public Map<String, Task> getTasks() {
        return tasks;
    }

    public void addTask(String jobName, Task task) {
        tasks.put(jobName, task);
    }

    public Task getTask(String jobName) {
        return tasks.get(jobName);
    }
}
