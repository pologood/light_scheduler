package com.jd.eptid.scheduler.client.test.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

/**
 * Created by classdan on 16-9-30.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring-config-context.xml"})
public class ClientBootstrapTest {

    @Test
    public void testStandby() throws InterruptedException {
        TimeUnit.HOURS.sleep(1);
    }

}
