package com.jd.eptid.scheduler.test.core;

import com.jd.eptid.scheduler.core.config.Configuration;
import com.jd.eptid.scheduler.server.config.ConfigItem;
import org.junit.Test;

/**
 * Created by classdan on 16-10-31.
 */
public class ConfigTest {

    @Test
    public void testLoad() {
        String value = Configuration.get(ConfigItem.BIZ_POOL_SIZE);
        System.out.println(value);
    }

}
