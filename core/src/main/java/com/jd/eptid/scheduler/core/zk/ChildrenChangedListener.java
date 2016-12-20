package com.jd.eptid.scheduler.core.zk;

import java.util.List;

/**
 * Created by classdan on 16-11-8.
 */
public interface ChildrenChangedListener {

    void onChange(String path, List<String> children);

}
