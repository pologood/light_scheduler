package com.jd.eptid.scheduler.core.master;

import com.jd.eptid.scheduler.core.domain.node.Node;

/**
 * Created by classdan on 16-10-31.
 */
public interface MasterChangeListener {

    void onChange(Node masterNode);

}
