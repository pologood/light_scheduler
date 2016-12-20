package com.jd.eptid.scheduler.test.core;

import com.jd.eptid.scheduler.server.core.ServerBootstrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Created by ClassDan on 2016/10/8.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring-config-context.xml"})
public class ServerBootstrapTest {
    @Resource
    private ServerBootstrap serverBootstrap;

    @Test
    public void testStandby() throws InterruptedException {
        TimeUnit.MINUTES.sleep(5);
    }

}
