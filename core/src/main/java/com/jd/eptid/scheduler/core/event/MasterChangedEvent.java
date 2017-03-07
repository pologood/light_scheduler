package com.jd.eptid.scheduler.core.event;

import com.jd.eptid.scheduler.core.domain.node.Node;

/**
 * Created by classdan on 16-12-27.
 */
public class MasterChangedEvent extends AbstractEvent {
    private Node masterNode;

    public MasterChangedEvent(Node masterNode) {
        super();
        this.masterNode = masterNode;
    }

    public Node getMasterNode() {
        return masterNode;
    }

    public void setMasterNode(Node masterNode) {
        this.masterNode = masterNode;
    }
}
