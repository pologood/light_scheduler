package com.jd.eptid.scheduler.client.master;

import com.jd.eptid.scheduler.client.core.ClientContext;
import com.jd.eptid.scheduler.core.event.MasterChangedEvent;
import com.jd.eptid.scheduler.core.event.ZNodeEvent;
import com.jd.eptid.scheduler.core.event.ZkEvent;
import com.jd.eptid.scheduler.core.event.ZkStateEvent;
import com.jd.eptid.scheduler.core.listener.MasterChangeListener;
import com.jd.eptid.scheduler.core.listener.ZkEventListener;
import com.jd.eptid.scheduler.core.master.MasterChooser;
import com.jd.eptid.scheduler.core.master.MasterPreemptor;
import com.jd.eptid.scheduler.core.zk.ZookeeperEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by classdan on 16-11-14.
 */
public class PreemptiveMasterWatcher implements MasterWatcher, ZkEventListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String MASTER_NODE_PATH = "/scheduler/nodes/server/master";
    private ZookeeperEndpoint zookeeperEndpoint;
    private MasterChooser masterChooser;

    public PreemptiveMasterWatcher() {
        ClientContext clientContext = ClientContext.getInstance();
        zookeeperEndpoint = clientContext.getZookeeperEndpoint();
        this.masterChooser = new MasterPreemptor(zookeeperEndpoint, clientContext.getEventBroadcaster());
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
                boolean isExists = zookeeperEndpoint.exists(MASTER_NODE_PATH);
                if (!isExists) {
                    logger.error("No master node found.");
                } else {
                    masterChooser.choose(Arrays.asList(MASTER_NODE_PATH));
                }

                zookeeperEndpoint.addNodeChangedListener(MASTER_NODE_PATH, this);
            }
        } else if (event instanceof ZNodeEvent) {
            ZNodeEvent nodeEvent = (ZNodeEvent) event;
            Assert.isTrue(nodeEvent.getPath().equals(MASTER_NODE_PATH));

            if (nodeEvent.getCode() == ZNodeEvent.Code.ADD) {
                masterChooser.choose(Arrays.asList(MASTER_NODE_PATH));
            } else if (nodeEvent.getCode() == ZNodeEvent.Code.REMOVED) {
                masterChooser.choose(Collections.<String>emptyList());
            }
        }
    }
}
