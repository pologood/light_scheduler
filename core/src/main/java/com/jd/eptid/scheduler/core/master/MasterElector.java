package com.jd.eptid.scheduler.core.master;

import com.jd.eptid.scheduler.core.event.EventBroadcaster;
import com.jd.eptid.scheduler.core.zk.ZookeeperEndpoint;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Created by classdan on 16-10-31.
 */
public class MasterElector extends ZkBasedMasterChooser {

    public MasterElector(ZookeeperEndpoint zookeeperEndpoint, EventBroadcaster eventBroadcaster) {
        super(zookeeperEndpoint, eventBroadcaster);
    }

    @Override
    public void choose(List<String> candidates) {
        if (candidates.isEmpty()) {
            setMaster(null);
            return;
        }

        Assert.notEmpty(candidates);
        String minSequenceChild = getMinSequenceChild(candidates);
        setMaster(minSequenceChild);
    }

    private String getMinSequenceChild(List<String> children) {
        Assert.notEmpty(children);

        String minSequenceChild = null;
        for (String child : children) {
            if (minSequenceChild == null) {
                minSequenceChild = child;
            }

            if (getSequence(child).compareTo(getSequence(minSequenceChild)) < 0) {
                minSequenceChild = child;
            }
        }
        return minSequenceChild;
    }

    private String getSequence(String nodeName) {
        int index = nodeName.lastIndexOf("-");
        return nodeName.substring(index);
    }
}
