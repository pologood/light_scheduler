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
import java.util.Random;

/**
 * Created by classdan on 17-1-17.
 */
public class LongJob implements Job<TestParam> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String name() {
        return "longJob";
    }

    @Override
    public SplitResult<TestParam> split(int splitTimes) {
        logger.info("Split job: {}.", splitTimes);
        List<TestParam> tasks = new ArrayList();
        int pageSize = 11;
        int start = (splitTimes - 1) * pageSize + 1;
        int end = splitTimes * pageSize;
        for (int i = start; i <= end; ++i) {
            TestParam param = new TestParam();
            param.setName("longTask" + i);
            param.setCode(12200 + i);
            TestNestedParam nestedParam = new TestNestedParam();
            nestedParam.setId(2939900L + i);
            nestedParam.setTime(new Date());
            param.setNestedParam(nestedParam);
            tasks.add(param);
        }
        SplitResult result = new SplitResult();
        result.setLast(splitTimes == 1000 ? true : false);
        result.setTaskParams(tasks);
        return result;
    }

    private int determinePageSize() {
        int max = 13;
        int min = 10;
        Random random = new Random();
        return random.nextInt(max) % (max - min + 1) + min;
    }

}
