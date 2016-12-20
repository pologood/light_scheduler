package com.jd.eptid.scheduler.client.master;

import com.jd.eptid.scheduler.client.core.AppContext;
import com.jd.eptid.scheduler.core.master.MasterChangeListener;
import com.jd.eptid.scheduler.core.master.MasterChooser;
import com.jd.eptid.scheduler.core.master.MasterElector;
import com.jd.eptid.scheduler.core.network.TransportReadyListener;
import com.jd.eptid.scheduler.core.zk.ChildrenChangedListener;
import com.jd.eptid.scheduler.core.zk.ZookeeperTransport;
import org.apache.commons.collections.CollectionUtils;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Created by classdan on 16-11-14.
 */
public class ElectiveMasterWatcher implements MasterWatcher, TransportReadyListener<ZooKeeper>, ChildrenChangedListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String SERVER_NODE_PATH = "/scheduler/nodes/server";
    private ZookeeperTransport zookeeperTransport;
    private MasterChooser masterChooser;

    public ElectiveMasterWatcher() {
        this.zookeeperTransport = AppContext.getInstance().getZookeeperTransport();
        this.masterChooser = new MasterElector(this.zookeeperTransport, AppContext.getInstance().thisNode());
        zookeeperTransport.addReadyListener(this);
    }

    @Override
    public void onReady(ZooKeeper zooKeeper) {
        List<String> children = zookeeperTransport.getChildren(SERVER_NODE_PATH);
        if (CollectionUtils.isEmpty(children)) {
            logger.error("No master node found.");
        } else {
            masterChooser.choose(children);
        }

        zookeeperTransport.addChildrenChangedListener(SERVER_NODE_PATH, this);
    }

    @Override
    public void onChange(String path, List<String> children) {
        Assert.isTrue(path.equals(SERVER_NODE_PATH));
        masterChooser.choose(children);
    }

    @Override
    public void addListener(MasterChangeListener listener) {
        masterChooser.addListener(listener);
    }
}
