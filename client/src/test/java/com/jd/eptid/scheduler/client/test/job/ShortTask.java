package com.jd.eptid.scheduler.client.test.job;

import com.alibaba.fastjson.JSON;
import com.jd.eptid.scheduler.client.core.Task;
import com.jd.eptid.scheduler.client.test.param.TestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by classdan on 16-10-27.
 */
public class ShortTask implements Task<TestParam> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String job() {
        return "shortJob";
    }

    @Override
    public void run(TestParam param) throws Exception {
        logger.info("Run task: {}.", JSON.toJSONString(param));
        TimeUnit.SECONDS.sleep(3);
        logger.info("Run task: {} successful.", JSON.toJSONString(param));
    }
}
