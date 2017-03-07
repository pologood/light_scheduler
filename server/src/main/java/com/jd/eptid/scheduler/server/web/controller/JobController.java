package com.jd.eptid.scheduler.server.web.controller;

import com.jd.eptid.scheduler.core.domain.job.Job;
import com.jd.eptid.scheduler.core.domain.job.OneShotJob;
import com.jd.eptid.scheduler.core.domain.job.PeriodicJob;
import com.jd.eptid.scheduler.core.domain.job.ScheduledJob;
import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.core.domain.task.TaskClient;
import com.jd.eptid.scheduler.core.failover.FailoverPolicy;
import com.jd.eptid.scheduler.server.dao.ScheduledJobDao;
import com.jd.eptid.scheduler.server.job.*;
import com.jd.eptid.scheduler.server.web.page.PageData;
import com.jd.eptid.scheduler.server.web.request.JobSaveRequest;
import com.jd.eptid.scheduler.server.web.response.Response;
import com.jd.eptid.scheduler.server.web.response.ScheduleInfo;
import com.jd.eptid.scheduler.server.web.response.SchedulingView;
import com.jd.eptid.scheduler.server.web.response.SubmittedView;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by classdan on 16-12-6.
 */
@Controller
@RequestMapping("/job")
public class JobController extends BaseController {
    @Resource
    private JobManager jobManager;
    @Resource
    private JobScheduler jobScheduler;
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
    public String newJobPage() {
        return "job/new";
    }

    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String editPage(long id) {
        Job job = jobManager.findJob(id);
        if (job == null) {
            return "error";
        }

        setRequestAttribute("jobId", id);
        setRequestAttribute("name", job.getName());
        setRequestAttribute("description", job.getDescription());
        if (job instanceof PeriodicJob) {
            PeriodicJob periodicJob = (PeriodicJob) job;
            setRequestAttribute("cron", true);
            setRequestAttribute("interval", periodicJob.getPeriodicInterval());
            setRequestAttribute("allowConcurrent", periodicJob.isAllowConcurrent());
        }
        return "job/new";
    }

    @RequestMapping(value = "/schedule/detail", method = RequestMethod.GET)
    public String scheduleDetailPage(@RequestParam("jobId") long jobId) {
        setRequestAttribute("jobId", jobId);
        return "job/scheduleDetail";
    }

    @ResponseBody
    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public Response<Long> newJob(@RequestBody JobSaveRequest jobSaveRequest) {
        long newJobId = jobManager.addJob(convert(jobSaveRequest));
        return Response.successResponse(newJobId);
    }

    @ResponseBody
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public Response<Long> editJob(@RequestBody JobSaveRequest jobSaveRequest) {
        try {
            jobManager.update(jobSaveRequest.getId(), convert(jobSaveRequest));
            return Response.successResponse(jobSaveRequest.getId());
        } catch (Exception e) {
            logger.error("Failed to edit job: {}.", jobSaveRequest.getId(), e);
            return Response.failureResponse(e.getMessage());
        }
    }

    private Job convert(JobSaveRequest jobSaveRequest) {
        Job job = null;
        if (jobSaveRequest.isCron()) {
            PeriodicJob periodicJob = new PeriodicJob();
            periodicJob.setStartTime(jobSaveRequest.getStartTime());
            periodicJob.setPeriodicInterval(jobSaveRequest.getInterval());
            periodicJob.setAllowConcurrent(jobSaveRequest.isAllowConcurrent());
            job = periodicJob;
        } else {
            OneShotJob oneShotJob = new OneShotJob();
            job = oneShotJob;
        }
        job.setId(jobSaveRequest.getId());
        job.setName(jobSaveRequest.getName());
        job.setDescription(jobSaveRequest.getDescription());
        if (ArrayUtils.isNotEmpty(jobSaveRequest.getMutexJobIds())) {
            job.setMutexJobIds(Arrays.asList(jobSaveRequest.getMutexJobIds()));
        }
        job.setFailoverPolicy(FailoverPolicy.valueOf(jobSaveRequest.getFailureStrategy()));
        return job;
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

    @ResponseBody
    @RequestMapping(value = "/enable", method = RequestMethod.POST)
    public Response<Boolean> enable(long jobId) {
        try {
            jobManager.enable(jobId);
            return Response.successResponse(true);
        } catch (Exception e) {
            logger.error("Failed to enable job: {}.", jobId, e);
            return Response.failureResponse(e.getMessage());
        }
    }

    @ResponseBody
    @RequestMapping(value = "/disable", method = RequestMethod.POST)
    public Response<Boolean> disable(long jobId) {
        try {
            jobManager.disable(jobId);
            return Response.successResponse(true);
        } catch (Exception e) {
            logger.error("Failed to disable job: {}.", jobId, e);
            return Response.failureResponse(e.getMessage());
        }
    }

    @ResponseBody
    @RequestMapping(value = "/schedule/list", method = RequestMethod.GET)
    public PageData<List<SubmittedView>> scheduleList(@RequestParam("draw") int pageNo, @RequestParam("length") int pageSize) {
        Map<Long, SubmittedJob> submittedJobs = jobScheduler.snapshot();
        List<SubmittedView> jobs = extractJobs(submittedJobs);
        return PageData.build(pageNo, jobs.size(), jobs);
    }

    private List<SubmittedView> extractJobs(Map<Long, SubmittedJob> submittedJobs) {
        List<SubmittedView> views = new ArrayList<SubmittedView>();
        for (Map.Entry<Long, SubmittedJob> entry : submittedJobs.entrySet()) {
            Job job = entry.getValue().getJob();

            SubmittedView view = new SubmittedView();
            view.setId(job.getId());
            view.setName(job.getName());
            view.setInterval(job instanceof PeriodicJob ? ((PeriodicJob) job).getPeriodicInterval() : null);
            view.setScheduling(entry.getValue().getJobTrackers().keySet());
            views.add(view);
        }
        return views;
    }

    @ResponseBody
    @RequestMapping(value = "/schedule/snapshot/{jobId}", method = RequestMethod.GET)
    public Response<SchedulingView> snapshotOfJob(@PathVariable("jobId") long jobId) {
        SubmittedJob submittedJob = jobScheduler.getSubmittedJob(jobId);

        SchedulingView view = new SchedulingView();
        view.setJob(submittedJob.getJob());
        Map<String, ScheduleInfo> scheduleInfoMap = new HashMap<String, ScheduleInfo>();
        for (Map.Entry<String, JobTracker> entry : submittedJob.getJobTrackers().entrySet()) {
            JobContext jobContext = submittedJob.getJobContext(entry.getKey());
            scheduleInfoMap.put(entry.getKey(), extractScheduleInfo(jobContext));
        }
        view.setScheduleInfo(scheduleInfoMap);
        return Response.successResponse(view);
    }

    private ScheduleInfo extractScheduleInfo(JobContext jobContext) {
        ScheduleInfo scheduleInfo = new ScheduleInfo();
        Client jobClient = jobContext.getJobClient();
        scheduleInfo.setJobClient(jobClient.getIp() + ":" + jobClient.getPort());
        Map<String, TaskClient> taskClients = jobContext.getTaskClients();
        scheduleInfo.setTaskClients(taskClients.values());
        return scheduleInfo;
    }

    @ResponseBody
    @RequestMapping(value = "/enableMonitorLog", method = RequestMethod.GET)
    public Response<Boolean> enableMonitorLog(@RequestParam("jobId") long jobId, @RequestParam("scheduleId") String scheduleId) {
        try {
            SubmittedJob submittedJob = jobScheduler.getSubmittedJob(jobId);
            JobTracker jobTracker = submittedJob.getJobTracker(scheduleId);
            jobTracker.getJobContext().enableMonitorLog();
            return Response.successResponse(true);
        } catch (Exception e) {
            logger.error("Failed to enable the monitor log: {}.", jobId, e);
            return Response.failureResponse(e.getMessage());
        }
    }

}
