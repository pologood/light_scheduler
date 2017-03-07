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
 * Created by ClassDan on 2016/10/8.
 */
public class TestJob implements Job<TestParam> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String name() {
        return "testJob";
    }

    @Override
    public SplitResult<TestParam> split(int splitTimes) {
        logger.info("Split job: {}.", splitTimes);
        List<TestParam> tasks = new ArrayList();
        int start = (splitTimes - 1) * 5 + 1;
        int end = splitTimes * 5;
        for (int i = start; i <= end; ++i) {
            TestParam param = new TestParam();
            param.setName("testTask" + i);
            param.setCode(2000 + i);
            TestNestedParam nestedParam = new TestNestedParam();
            nestedParam.setId(90000L + i);
            nestedParam.setTime(new Date());
            param.setNestedParam(nestedParam);
            tasks.add(param);
        }
        SplitResult result = new SplitResult();
        result.setLast(splitTimes == 2 ? true : false);
        result.setTaskParams(tasks);
        return result;
    }

}
