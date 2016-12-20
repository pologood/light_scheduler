package com.jd.eptid.scheduler.server.web.controller;

import com.jd.eptid.scheduler.core.domain.job.Job;
import com.jd.eptid.scheduler.core.domain.job.ScheduledJob;
import com.jd.eptid.scheduler.server.dao.ScheduledJobDao;
import com.jd.eptid.scheduler.server.job.JobManager;
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
 * Created by classdan on 16-12-6.
 */
@Controller
@RequestMapping("/job")
public class JobController extends BaseController {
    @Resource
    private JobManager jobManager;
    @Resource
    private ScheduledJobDao scheduledJobDao;

    @RequestMapping(value = "/manage", method = RequestMethod.GET)
    public String managementPage() {
        return "job/manage";
    }

    @RequestMapping(value = "/monitor", method = RequestMethod.GET)
    public String monitorPage() {
        return "job/monitor";
    }

    @RequestMapping(value = "/history", method = RequestMethod.GET)
    public String historyPage() {
        return "job/history";
    }

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String newPage() {
        return "job/new";
    }

    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public PageData<List<Job>> list(@RequestParam("draw") int pageNo, @RequestParam("length") int pageSize) {
        int total = jobManager.count();
        List<Job> allJobs = null;
        if (total > 0) {
            allJobs = jobManager.getAllJobs();
        } else {
            allJobs = Collections.emptyList();
        }
        return PageData.build(pageNo, total, allJobs);
    }

    @ResponseBody
    @RequestMapping(value = "/scheduled", method = RequestMethod.GET)
    public PageData<List<ScheduledJob>> scheduledHistory(@RequestParam("draw") int pageNo, @RequestParam("length") int pageSize) {
        int total = scheduledJobDao.count();
        List<ScheduledJob> scheduledJobs = null;
        if (total > 0) {
            int rowNo = pageNo == 1 ? 0 : (pageNo - 1) * pageSize;
            scheduledJobs = scheduledJobDao.getByPage(rowNo, pageSize);
        } else {
            scheduledJobs = Collections.emptyList();
        }
        return PageData.build(pageNo, total, scheduledJobs);
    }

    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public String newJob() {
        return null;
    }

}
