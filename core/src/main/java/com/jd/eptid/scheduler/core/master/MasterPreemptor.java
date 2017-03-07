package com.jd.eptid.scheduler.core.master;

import com.jd.eptid.scheduler.core.event.EventBroadcaster;
import com.jd.eptid.scheduler.core.zk.ZookeeperEndpoint;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Created by classdan on 16-11-9.
 */
public class MasterPreemptor extends ZkBasedMasterChooser {

    public MasterPreemptor(ZookeeperEndpoint zookeeperEndpoint, EventBroadcaster eventBroadcaster) {
        super(zookeeperEndpoint, eventBroadcaster);
    }

    @Override
    public void choose(List<String> candidates) {
        if (CollectionUtils.isEmpty(candidates)) {
            setMaster(null);
            return;
        }

        Assert.isTrue(candidates.size() == 1);
        setMaster(candidates.get(0));
    }

}
