package com.jd.eptid.scheduler.client.master;

import com.jd.eptid.scheduler.client.core.AppContext;
import com.jd.eptid.scheduler.core.domain.Event;
import com.jd.eptid.scheduler.core.master.MasterChangeListener;
import com.jd.eptid.scheduler.core.master.MasterChooser;
import com.jd.eptid.scheduler.core.master.MasterPreemptor;
import com.jd.eptid.scheduler.core.network.TransportReadyListener;
import com.jd.eptid.scheduler.core.zk.NodeChangedListener;
import com.jd.eptid.scheduler.core.zk.ZookeeperTransport;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Arrays;

/**
 * Created by classdan on 16-11-14.
 */
public class PreemptiveMasterWatcher implements MasterWatcher, TransportReadyListener<ZooKeeper>, NodeChangedListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String MASTER_NODE_PATH = "/scheduler/nodes/server/master";
    private ZookeeperTransport zookeeperTransport;
    private MasterChooser masterChooser;

    public PreemptiveMasterWatcher() {
        zookeeperTransport = AppContext.getInstance().getZookeeperTransport();
        this.masterChooser = new MasterPreemptor(zookeeperTransport, AppContext.getInstance().thisNode());
        zookeeperTransport.addReadyListener(this);
    }

    @Override
    public void onChange(String path, Event event) {
        Assert.isTrue(path.equals(MASTER_NODE_PATH));

        if (event == Event.ADD) {
            masterChooser.choose(Arrays.asList(MASTER_NODE_PATH));
        }
    }

    @Override
    public void onReady(ZooKeeper zooKeeper) {
        boolean isExists = zookeeperTransport.exists(MASTER_NODE_PATH);
        if (!isExists) {
            logger.error("No master node found.");
        } else {
            masterChooser.choose(Arrays.asList(MASTER_NODE_PATH));
        }

        zookeeperTransport.addNodeChangedListener(MASTER_NODE_PATH, this);
    }

    @Override
    public void addListener(MasterChangeListener listener) {
        masterChooser.addListener(listener);
    }
}
