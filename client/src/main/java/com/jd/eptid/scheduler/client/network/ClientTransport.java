package com.jd.eptid.scheduler.client.network;


import com.jd.eptid.scheduler.core.domain.message.Message;
import com.jd.eptid.scheduler.core.network.Transport;

/**
 * Created by classdan on 16-9-14.
 */
public interface ClientTransport extends Transport {

    void connect();

    void disconnect();

    void send(Message message);

}
