package com.jd.eptid.scheduler.core.master;

import com.jd.eptid.scheduler.core.domain.node.Node;
import com.jd.eptid.scheduler.core.zk.ZookeeperTransport;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Created by classdan on 16-11-9.
 */
public class MasterPreemptor extends AbstractMasterChooser {

    public MasterPreemptor(ZookeeperTransport zookeeperTransport, Node thisNode) {
        super(zookeeperTransport, thisNode);
    }

    @Override
    public void choose(List<String> candidates) {
        Assert.notEmpty(candidates);
        Assert.isTrue(candidates.size() == 1);

        setMaster(candidates.get(0));
    }

}
