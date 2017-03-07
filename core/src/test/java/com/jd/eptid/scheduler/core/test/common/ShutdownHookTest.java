package com.jd.eptid.scheduler.core.test.common;

import com.jd.eptid.scheduler.core.common.LifeCycle;
import com.jd.eptid.scheduler.core.common.ShutdownHook;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created by classdan on 16-12-28.
 */
public class ShutdownHookTest {

    @Test
    public void testShutdown() throws InterruptedException {
        ShutdownHook.getInstance().addLifeCycleObject(new LifeCycleObj(3));
        ShutdownHook.getInstance().addLifeCycleObject(new LifeCycleObj(6));
        ShutdownHook.getInstance().addLifeCycleObject(new LifeCycleObj(1));
        ShutdownHook.getInstance().addLifeCycleObject(new LifeCycleObj(0));
        ShutdownHook.getInstance().addLifeCycleObject(new LifeCycleObj(1));
        ShutdownHook.getInstance().addLifeCycleObject(new LifeCycleObj(3));
        ShutdownHook.getInstance().addLifeCycleObject(new LifeCycleObj(3));
        ShutdownHook.getInstance().addLifeCycleObject(new LifeCycleObj(5));

        TimeUnit.SECONDS.sleep(3);
    }

    class LifeCycleObj implements LifeCycle {
        private int order;

        public LifeCycleObj(int order) {
            this.order = order;
        }

        @Override
        public void start() {
            System.out.println("LifeCycle object " + order + " started.");
        }

        @Override
        public void stop() {
            System.out.println("LifeCycle object " + order + " stopped.");
        }

        @Override
        public int order() {
            return order;
        }
    }

}
