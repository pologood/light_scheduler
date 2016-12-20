package com.jd.eptid.scheduler.core.master;


import java.util.List;

/**
 * Created by classdan on 16-11-9.
 */
public interface MasterChooser {

    void choose(List<String> candidates);

    boolean isMaster();

    void addListener(MasterChangeListener listener);

}
