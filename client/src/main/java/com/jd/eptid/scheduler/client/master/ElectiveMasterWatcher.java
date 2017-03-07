package com.jd.eptid.scheduler.client.master;

import com.jd.eptid.scheduler.client.core.ClientContext;
import com.jd.eptid.scheduler.core.event.MasterChangedEvent;
import com.jd.eptid.scheduler.core.event.ZChildrenEvent;
import com.jd.eptid.scheduler.core.event.ZkEvent;
import com.jd.eptid.scheduler.core.event.ZkStateEvent;
import com.jd.eptid.scheduler.core.listener.MasterChangeListener;
import com.jd.eptid.scheduler.core.listener.ZkEventListener;
import com.jd.eptid.scheduler.core.master.MasterChooser;
import com.jd.eptid.scheduler.core.master.MasterElector;
import com.jd.eptid.scheduler.core.zk.ZookeeperEndpoint;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Created by classdan on 16-11-14.
 */
public class ElectiveMasterWatcher implements MasterWatcher, ZkEventListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String SERVER_NODE_PATH = "/scheduler/nodes/server";
    private ZookeeperEndpoint zookeeperEndpoint;
    private MasterChooser masterChooser;

    public ElectiveMasterWatcher() {
        ClientContext clientContext = ClientContext.getInstance();
        this.zookeeperEndpoint = clientContext.getZookeeperEndpoint();
        this.masterChooser = new MasterElector(this.zookeeperEndpoint, clientContext.getEventBroadcaster());
        zookeeperEndpoint.addStateListener(this);
    }

    @Override
    public void addListener(MasterChangeListener listener) {
        ClientContext.getInstance().getEventBroadcaster().register(MasterChangedEvent.class, listener);
    }

    @Override
    public void onEvent(ZkEvent event) {
        if (event instanceof ZkStateEvent) {
            ZkStateEvent stateEvent = (ZkStateEvent) event;
            if (stateEvent.getCode() == ZkStateEvent.Code.CONNECTED) {
                List<String> children = zookeeperEndpoint.getChildren(SERVER_NODE_PATH);
                if (CollectionUtils.isEmpty(children)) {
                    logger.error("No master node found.");
                } else {
                    masterChooser.choose(children);
                }

                zookeeperEndpoint.addChildrenChangedListener(SERVER_NODE_PATH, this);
            }
        } else if (event instanceof ZChildrenEvent) {
            ZChildrenEvent childrenEvent = (ZChildrenEvent) event;
            Assert.isTrue(childrenEvent.getPath().equals(SERVER_NODE_PATH));
            masterChooser.choose(childrenEvent.getChildren());
        }
    }
}
