package com.jd.eptid.scheduler.core.zk;

import com.jd.eptid.scheduler.core.domain.node.Node;

/**
 * Created by classdan on 16-11-9.
 */
public class ZNode {
    private Node node;
    private int sequence;

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}
