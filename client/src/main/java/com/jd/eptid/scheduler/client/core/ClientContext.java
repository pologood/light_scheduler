package com.jd.eptid.scheduler.client.core;

import com.google.common.base.Throwables;
import com.jd.eptid.scheduler.client.master.MasterWatcher;
import com.jd.eptid.scheduler.client.network.ClientTransport;
import com.jd.eptid.scheduler.client.registry.ClientRegistry;
import com.jd.eptid.scheduler.core.common.Context;
import com.jd.eptid.scheduler.core.domain.message.MessageType;
import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.core.domain.node.Node;
import com.jd.eptid.scheduler.core.processor.MessageProcessor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by classdan on 16-9-30.
 */
public class ClientContext extends Context {
    private static ClientContext clientContext = new ClientContext();
    private ClientTransport clientTransport;
    private Node thisNode;
    private Node masterNode;
    private ClientRegistry clientRegistry;
    private MasterWatcher masterWatcher;
    private final Map<MessageType, MessageProcessor> messageProcessors = new HashMap<MessageType, MessageProcessor>();
    private final Map<String, Job> jobs = new HashMap<String, Job>();
    private final Map<String, Task> tasks = new HashMap<String, Task>();

    private ClientContext() {
        Client node = new Client();
        node.setSupportJobs(new HashSet<String>());
        node.setCreateTime(System.currentTimeMillis());
        thisNode = node;
    }

    public static ClientContext getInstance() {
        return clientContext;
    }

    public ClientTransport getClientTransport() {
        return clientTransport;
    }

    public void setClientTransport(ClientTransport clientTransport) {
        this.clientTransport = clientTransport;
    }

    public void changeNode(String ip, int port) {
        Assert.notNull(thisNode);
        synchronized (thisNode) {
            thisNode.setIp(ip);
            thisNode.setPort(port);
            thisNode.notifyAll();
        }
    }

    public void resetThisNode() {
        synchronized (thisNode) {
            thisNode.setIp(null);
            thisNode.setPort(0);
        }
    }

    public Node thisNode() {
        synchronized (thisNode) {
            if (StringUtils.isBlank(thisNode.getIp())) {
                try {
                    thisNode.wait();
                } catch (InterruptedException e) {
                    Throwables.propagate(e);
                }
            }
        }
        return thisNode;
    }

    public Node getMasterNode() {
        return masterNode;
    }

    public void setMasterNode(Node masterNode) {
        this.masterNode = masterNode;
    }

    public ClientRegistry getClientRegistry() {
        return clientRegistry;
    }

    public void setClientRegistry(ClientRegistry clientRegistry) {
        this.clientRegistry = clientRegistry;
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
