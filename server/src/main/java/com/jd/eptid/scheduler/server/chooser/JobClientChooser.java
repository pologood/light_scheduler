package com.jd.eptid.scheduler.server.chooser;

import com.jd.eptid.scheduler.core.event.ClientEvent;
import com.jd.eptid.scheduler.core.listener.EventListener;

/**
 * Created by classdan on 17-1-17.
 */
public interface JobClientChooser extends ClientChooser, EventListener<ClientEvent> {
}
