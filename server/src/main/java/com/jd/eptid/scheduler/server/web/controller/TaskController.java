package com.jd.eptid.scheduler.server.web.controller;

import com.jd.eptid.scheduler.core.domain.task.ScheduledTask;
import com.jd.eptid.scheduler.server.dao.ScheduledTaskDao;
import com.jd.eptid.scheduler.server.web.page.PageData;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * Created by classdan on 16-12-7.
 */
@Controller
@RequestMapping("/task")
public class TaskController extends BaseController {
    @Resource
    private ScheduledTaskDao scheduledTaskDao;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String listPage(@RequestParam("jobScheduleId") long jobScheduleId) {
        setRequestAttribute("jobScheduleId", jobScheduleId);
        return "task/list";
    }

    @ResponseBody
    @RequestMapping(value = "/scheduled", method = RequestMethod.GET)
    public PageData<List<ScheduledTask>> list(@RequestParam("jobScheduleId") long jobScheduleId, @RequestParam("draw") int pageNo, @RequestParam("length") int pageSize) {
        int total = scheduledTaskDao.count(jobScheduleId);
        List<ScheduledTask> scheduledTasks = null;
        if (total > 0) {
            int rowNo = pageNo == 1 ? 0 : (pageNo - 1) * pageSize;
            scheduledTasks = scheduledTaskDao.findByJobId(jobScheduleId, rowNo, pageSize);
        } else {
            scheduledTasks = Collections.emptyList();
        }
        return PageData.build(pageNo, total, scheduledTasks);
    }

}
