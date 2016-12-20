package com.jd.eptid.scheduler.server.chooser;


import com.jd.eptid.scheduler.core.domain.node.Client;

/**
 * Created by classdan on 16-9-22.
 */
public interface ClientChooser {

    Client chooseAndOccupy(String jobName);

    void release(String jobName, Client client);

}
