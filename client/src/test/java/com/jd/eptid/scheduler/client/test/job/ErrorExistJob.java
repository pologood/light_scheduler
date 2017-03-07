package com.jd.eptid.scheduler.client.test.job;

import com.jd.eptid.scheduler.client.core.Job;
import com.jd.eptid.scheduler.client.core.SplitResult;
import com.jd.eptid.scheduler.client.test.param.TestNestedParam;
import com.jd.eptid.scheduler.client.test.param.TestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by classdan on 17-1-17.
 */
public class ErrorExistJob implements Job<TestParam> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String name() {
        return "errorExistJob";
    }

    @Override
    public SplitResult<TestParam> split(int splitTimes) {
        logger.info("Split job: {}.", splitTimes);
        List<TestParam> tasks = new ArrayList();
        for (int i = 0; i <= 3; ++i) {
            /*if (i == 2) {
                throw new RuntimeException("Error: split error, 39921.");
            }*/

            TestParam param = new TestParam();
            param.setName("mayFailedTask" + i);
            param.setCode(8000 + i);
            TestNestedParam nestedParam = new TestNestedParam();
            nestedParam.setId(91200L + i);
            nestedParam.setTime(new Date());
            param.setNestedParam(nestedParam);
            tasks.add(param);
        }
        SplitResult result = new SplitResult();
        result.setLast(true);
        result.setTaskParams(tasks);
        return result;
    }
}
