package com.jd.eptid.scheduler.test;

import com.jd.eptid.scheduler.server.core.ServerBootstrap;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by ClassDan on 2016/10/8.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring-config-context.xml"})
public abstract class BaseTest {
    @Resource
    private ServerBootstrap serverBootstrap;

}