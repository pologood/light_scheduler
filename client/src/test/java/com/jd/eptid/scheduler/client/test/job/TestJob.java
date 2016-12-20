package com.jd.eptid.scheduler.client.test.job;

import com.jd.eptid.scheduler.client.core.Job;
import com.jd.eptid.scheduler.client.core.SplitResult;
import com.jd.eptid.scheduler.client.test.param.TestNestedParam;
import com.jd.eptid.scheduler.client.test.param.TestParam;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ClassDan on 2016/10/8.
 */
public class TestJob implements Job<TestParam> {

    @Override
    public String name() {
        return "testJob";
    }

    @Override
    public SplitResult<TestParam> split(int splitTimes) {
        List<TestParam> tasks = new ArrayList();
        TestParam param = new TestParam();
        param.setName("testTask1");
        param.setCode(2222);
        TestNestedParam nestedParam = new TestNestedParam();
        nestedParam.setId(92838L);
        nestedParam.setTime(new Date());
        param.setNestedParam(nestedParam);
        tasks.add(param);

        param = new TestParam();
        param.setName("testTask2");
        param.setCode(4444);
        nestedParam = new TestNestedParam();
        nestedParam.setId(93488L);
        nestedParam.setTime(new Date());
        param.setNestedParam(nestedParam);
        tasks.add(param);

        SplitResult result = new SplitResult();
        result.setLast(true);
        result.setTaskParams(tasks);
        return result;
    }

}
