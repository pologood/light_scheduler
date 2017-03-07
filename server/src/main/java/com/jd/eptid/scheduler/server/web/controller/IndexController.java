package com.jd.eptid.scheduler.server.web.controller;

import com.jd.eptid.scheduler.core.domain.job.JobStatus;
import com.jd.eptid.scheduler.server.core.ClientManager;
import com.jd.eptid.scheduler.server.dao.ScheduledJobDao;
import com.jd.eptid.scheduler.server.job.JobManager;
import com.jd.eptid.scheduler.server.web.response.Response;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by classdan on 16-12-6.
 */
@Controller
public class IndexController extends BaseController {
    @Resource
    private JobManager jobManager;
    @Resource
    private ClientManager clientManager;
    @Resource
    private ScheduledJobDao scheduledJobDao;

    @RequestMapping(value = {"", "/", "/index"})
    public String index() {
        return "redirect:/dashboard";
    }

    @RequestMapping(value = {"/dashboard"})
    public String dashboard() {
        return "index";
    }

    @ResponseBody
    @RequestMapping(value = "/statistics", method = RequestMethod.GET)
    public Response<Map<String, Object>> statistics() {
        int jobCount = jobManager.count();
        int clientCount = clientManager.count();
        int successSchedules = scheduledJobDao.countByStatus(JobStatus.SUCCESS.getCode());
        int totalSchedules = scheduledJobDao.count();
        double successRate = (double) successSchedules / totalSchedules;

        Map<String, Object> statistics = new HashMap<String, Object>();
        statistics.put("jobCount", jobCount);
        statistics.put("clientCount", clientCount);
        statistics.put("totalSchedules", totalSchedules);
        statistics.put("successRate", successRate);
        return Response.successResponse(statistics);
    }

}
